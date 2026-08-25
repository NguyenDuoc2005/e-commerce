package com.ecommerce.order.service.impl;

import com.ecommerce.order.client.PayoutClient;
import com.ecommerce.order.client.NotificationClient;
import com.ecommerce.order.client.CatalogClient;
import com.ecommerce.common.catalog.CatalogVariantSnapshot;
import com.ecommerce.order.constant.OrderStatusConstant;
import com.ecommerce.order.repository.OrderSellerRepository;
import com.ecommerce.order.service.SellerOrderService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.time.LocalDate;
import java.time.ZoneId;

@Service
public class SellerOrderServiceImpl implements SellerOrderService {

    private final JdbcTemplate jdbcTemplate;
    private final OrderSellerRepository orderSellerRepository;
    private final PayoutClient payoutClient;
    private final NotificationClient notificationClient;
    private final CatalogClient catalogClient;

    public SellerOrderServiceImpl(JdbcTemplate jdbcTemplate, OrderSellerRepository orderSellerRepository,
                                  PayoutClient payoutClient, NotificationClient notificationClient,
                                  CatalogClient catalogClient) {
        this.jdbcTemplate = jdbcTemplate;
        this.orderSellerRepository = orderSellerRepository;
        this.payoutClient = payoutClient;
        this.notificationClient = notificationClient;
        this.catalogClient = catalogClient;
    }

    @Override
    public List<Map<String, Object>> list(String sellerId, Integer status, String q) {
        String keyword = q == null ? "" : q.trim();
        return jdbcTemplate.queryForList("""
                SELECT os.id, os.order_id, o.code AS order_code, os.seller_id, os.shop_name, os.seller_slug,
                       os.total_amount, os.shipping_fee, os.discount_amount, os.total_after_discount,
                       os.order_status, os.created_date, o.customer_id, o.customer_name AS receiver_name,
                       o.customer_phone AS receiver_phone, o.shipping_address
                FROM order_seller os
                JOIN orders o ON o.id = os.order_id
                WHERE os.seller_id = ?
                  AND (? IS NULL OR os.order_status = ?)
                  AND (? = '' OR LOWER(o.code) LIKE LOWER(CONCAT('%', ?, '%')) OR LOWER(o.receiver_name) LIKE LOWER(CONCAT('%', ?, '%')))
                ORDER BY os.created_date DESC
                """, sellerId, status, status, keyword, keyword, keyword);
    }

    @Override
    public Map<String, Object> detail(String sellerId, String orderSellerId) {
        Map<String, Object> order = requireSellerOrder(sellerId, orderSellerId);
        List<Map<String, Object>> items = jdbcTemplate.queryForList("""
                SELECT oi.id, oi.product_variant_id, oi.quantity, oi.sale_price, oi.seller_id, oi.order_seller_id
                FROM order_item oi
                WHERE oi.order_seller_id = ?
                ORDER BY oi.created_date ASC
                """, orderSellerId);
        Map<String, Object> result = new LinkedHashMap<>(order);
        result.put("items", items);
        return result;
    }

    @Override
    @Transactional
    public Map<String, Object> changeStatus(String sellerId, String orderSellerId, String action) {
        Map<String, Object> order = requireSellerOrder(sellerId, orderSellerId);
        int current = ((Number) order.get("order_status")).intValue();
        int next = nextStatus(current, action);
        jdbcTemplate.update("UPDATE order_seller SET order_status = ? WHERE id = ? AND seller_id = ?", next, orderSellerId, sellerId);
        if (next == OrderStatusConstant.HOAN_THANH.ordinal()) {
            createPayoutReceivable(order, next);
        }
        notifyBuyer(order, next);
        return detail(sellerId, orderSellerId);
    }

    @Override
    public Map<String, Object> dashboard(String sellerId) {
        ZoneId zone = ZoneId.systemDefault();
        LocalDate today = LocalDate.now(zone);
        long startOfDay = today.atStartOfDay(zone).toInstant().toEpochMilli();
        long startOfWeek = today.minusDays(today.getDayOfWeek().getValue() - 1L).atStartOfDay(zone).toInstant().toEpochMilli();
        long startOfMonth = today.withDayOfMonth(1).atStartOfDay(zone).toInstant().toEpochMilli();
        long now = System.currentTimeMillis();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("todayRevenue", completedRevenue(sellerId, startOfDay, now));
        result.put("weekRevenue", completedRevenue(sellerId, startOfWeek, now));
        result.put("monthRevenue", completedRevenue(sellerId, startOfMonth, now));
        result.put("pendingOrders", countByStatus(sellerId, OrderStatusConstant.CHO_XAC_NHAN.ordinal()));
        result.put("shippingOrders", countByStatuses(sellerId, List.of(
                OrderStatusConstant.CHO_GIAO.ordinal(), OrderStatusConstant.DANG_GIAO.ordinal())));
        result.put("completedOrders", countByStatus(sellerId, OrderStatusConstant.HOAN_THANH.ordinal()));
        result.put("revenueSeries", jdbcTemplate.queryForList("""
                SELECT DATE_FORMAT(FROM_UNIXTIME(created_date / 1000), '%Y-%m-%d') AS date,
                       COALESCE(SUM(total_after_discount), 0) AS revenue
                FROM order_seller
                WHERE seller_id = ? AND order_status = ? AND created_date >= ?
                GROUP BY DATE_FORMAT(FROM_UNIXTIME(created_date / 1000), '%Y-%m-%d')
                ORDER BY date
                """, sellerId, OrderStatusConstant.HOAN_THANH.ordinal(), today.minusDays(6).atStartOfDay(zone).toInstant().toEpochMilli()));
        return result;
    }

    @Override
    public Map<String, Long> soldCounts(List<String> sellerIds) {
        if (sellerIds == null || sellerIds.isEmpty()) return Map.of();
        List<String> ids = sellerIds.stream().filter(Objects::nonNull).map(String::trim)
                .filter(id -> !id.isBlank()).distinct().toList();
        if (ids.isEmpty()) return Map.of();
        Map<String, Long> result = new LinkedHashMap<>();
        ids.forEach(id -> result.put(id, 0L));
        orderSellerRepository.findSoldCounts(ids, OrderStatusConstant.HOAN_THANH.ordinal())
                .forEach(row -> result.put(row.getSellerId(), row.getSoldCount() == null ? 0L : row.getSoldCount()));
        return result;
    }

    private double completedRevenue(String sellerId, long start, long end) {
        Double value = jdbcTemplate.queryForObject("""
                SELECT COALESCE(SUM(total_after_discount), 0) FROM order_seller
                WHERE seller_id = ? AND order_status = ? AND created_date BETWEEN ? AND ?
                """, Double.class, sellerId, OrderStatusConstant.HOAN_THANH.ordinal(), start, end);
        return value == null ? 0D : value;
    }

    private long countByStatus(String sellerId, int status) {
        Long value = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM order_seller WHERE seller_id = ? AND order_status = ?",
                Long.class, sellerId, status);
        return value == null ? 0L : value;
    }

    private long countByStatuses(String sellerId, List<Integer> statuses) {
        Long value = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM order_seller WHERE seller_id = ? AND order_status IN (?, ?)",
                Long.class, sellerId, statuses.get(0), statuses.get(1));
        return value == null ? 0L : value;
    }

    private Map<String, Object> requireSellerOrder(String sellerId, String orderSellerId) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT os.id, os.order_id, o.code AS order_code, os.seller_id, os.shop_name, os.seller_slug,
                       os.total_amount, os.shipping_fee, os.discount_amount, os.total_after_discount,
                       os.order_status, os.created_date, o.customer_id, o.customer_name AS receiver_name,
                       o.customer_phone AS receiver_phone, o.email, o.shipping_address
                FROM order_seller os
                JOIN orders o ON o.id = os.order_id
                WHERE os.id = ? AND os.seller_id = ?
                """, orderSellerId, sellerId);
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("Khong tim thay sub-order cua seller");
        }
        return rows.get(0);
    }

    private int nextStatus(int current, String action) {
        int confirm = OrderStatusConstant.DA_XAC_NHAN.ordinal();
        int readyToShip = OrderStatusConstant.CHO_GIAO.ordinal();
        int shipping = OrderStatusConstant.DANG_GIAO.ordinal();
        int completed = OrderStatusConstant.HOAN_THANH.ordinal();
        int cancelled = OrderStatusConstant.DA_HUY.ordinal();
        return switch (action) {
            case "confirm" -> requireTransition(current, OrderStatusConstant.CHO_XAC_NHAN.ordinal(), confirm);
            case "ready-to-ship" -> requireTransition(current, confirm, readyToShip);
            case "shipping" -> requireTransition(current, readyToShip, shipping);
            case "complete" -> requireTransition(current, shipping, completed);
            case "cancel" -> current == completed ? fail() : cancelled;
            default -> throw new IllegalArgumentException("Thao tac trang thai khong hop le");
        };
    }

    private int requireTransition(int current, int expected, int next) {
        if (current != expected) {
            throw new IllegalArgumentException("Trang thai hien tai khong cho phep thao tac nay");
        }
        return next;
    }

    private int fail() {
        throw new IllegalArgumentException("Khong the huy sub-order da hoan thanh");
    }

    private void createPayoutReceivable(Map<String, Object> order, int status) {
        double itemTotal = ((Number) order.get("total_amount")).doubleValue();
        double discount = order.get("discount_amount") == null
                ? 0D : ((Number) order.get("discount_amount")).doubleValue();
        double gross = Math.max(0D, itemTotal - discount);
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("orderSellerId", order.get("id"));
        payload.put("orderId", order.get("order_id"));
        payload.put("sellerId", order.get("seller_id"));
        payload.put("grossAmount", gross);
        payload.put("commissionLines", commissionLines(String.valueOf(order.get("id")), gross));
        payload.put("orderStatus", status);
        payoutClient.createReceivable(payload);
    }

    private List<Map<String, Object>> commissionLines(String orderSellerId, double gross) {
        List<Map<String, Object>> items = jdbcTemplate.queryForList("""
                SELECT product_variant_id, quantity, sale_price
                FROM order_item
                WHERE order_seller_id = ?
                """, orderSellerId);
        Map<String, Double> rawByCategory = new LinkedHashMap<>();
        for (Map<String, Object> item : items) {
            CatalogVariantSnapshot variant = catalogClient.getProductVariant(String.valueOf(item.get("product_variant_id")));
            String categoryId = variant.categoryId();
            double subtotal = ((Number) item.get("sale_price")).doubleValue()
                    * ((Number) item.get("quantity")).intValue();
            rawByCategory.merge(categoryId, subtotal, Double::sum);
        }
        double rawTotal = rawByCategory.values().stream().mapToDouble(Double::doubleValue).sum();
        if (rawTotal <= 0D) return List.of();

        List<Map<String, Object>> result = new java.util.ArrayList<>();
        double allocated = 0D;
        int index = 0;
        for (Map.Entry<String, Double> entry : rawByCategory.entrySet()) {
            index++;
            double lineGross = index == rawByCategory.size()
                    ? gross - allocated
                    : Math.round(gross * entry.getValue() / rawTotal * 100D) / 100D;
            allocated += lineGross;
            Map<String, Object> line = new LinkedHashMap<>();
            line.put("categoryId", entry.getKey());
            line.put("grossAmount", lineGross);
            result.add(line);
        }
        return result;
    }

    private void notifyBuyer(Map<String, Object> order, int status) {
        Object email = order.get("email");
        if (email == null || String.valueOf(email).isBlank()) {
            return;
        }
        try {
            String state = OrderStatusConstant.values()[status].name();
            notificationClient.sendEmail(Map.of(
                    "to", String.valueOf(email),
                    "subject", "Cap nhat don hang " + order.get("order_code"),
                    "content", "Kien hang cua shop " + order.get("shop_name") + " da chuyen sang trang thai " + state
            ));
        } catch (Exception ignored) {
            // Notification availability must not roll back order status changes.
        }
    }
}
