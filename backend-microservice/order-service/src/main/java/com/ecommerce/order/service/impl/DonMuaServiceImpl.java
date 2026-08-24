package com.ecommerce.order.service.impl;

import com.ecommerce.common.base.ResponseObject;
import com.ecommerce.common.catalog.CatalogVariantSnapshot;
import com.ecommerce.order.constant.OrderTypeConstant;
import com.ecommerce.order.constant.OrderStatusConstant;
import com.ecommerce.order.client.CatalogClient;
import com.ecommerce.order.client.PromotionClient;
import com.ecommerce.order.model.request.ChangeStatusRequest;
import com.ecommerce.order.model.request.OrderDetailRequest;
import com.ecommerce.order.model.request.OrderSearchRequest;
import com.ecommerce.order.model.request.ProductVariantSearchRequest;
import com.ecommerce.order.model.request.ThemProductRequest;
import com.ecommerce.order.model.request.UpdateDeliveryRequest;
import com.ecommerce.order.service.DonMuaService;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class DonMuaServiceImpl implements DonMuaService {

    private static final int ONLINE = OrderTypeConstant.ONLINE.ordinal();
    private static final int CHO_XAC_NHAN = OrderStatusConstant.CHO_XAC_NHAN.ordinal();
    private static final int DA_HUY = OrderStatusConstant.DA_HUY.ordinal();
    private static final int LUU_TAM = OrderStatusConstant.LUU_TAM.ordinal();

    private final JdbcTemplate jdbcTemplate;
    private final CatalogClient catalogClient;
    private final PromotionClient promotionClient;

    public DonMuaServiceImpl(JdbcTemplate jdbcTemplate, CatalogClient catalogClient, PromotionClient promotionClient) {
        this.jdbcTemplate = jdbcTemplate;
        this.catalogClient = catalogClient;
        this.promotionClient = promotionClient;
    }

    @Override
    public ResponseObject<?> getAllOrder(OrderSearchRequest request) {
        try {
            String q = like(request.getQ());
            Integer status = request.getStatus() == null ? null : request.getStatus().ordinal();
            List<Map<String, Object>> page = jdbcTemplate.queryForList("""
                    SELECT hd.id AS id,
                           hd.code AS maOrder,
                           hdct.product_variant_id AS idSPCT,
                           hdct.quantity AS quantity,
                           hdct.sale_price AS salePrice,
                           hd.order_status AS status,
                           hd.total_after_discount AS tongTien
                    FROM orders hd
                    JOIN order_item hdct ON hdct.order_id = hd.id
                    WHERE (? IS NULL OR ? = '' OR LOWER(hd.customer_id) LIKE LOWER(?))
                      AND (? IS NULL OR hd.order_status = ?)
                      AND hd.order_type = ?
                      AND hd.order_status != ?
                    ORDER BY hd.created_date, hd.code ASC
                    """, q, q, q, status, status, ONLINE, LUU_TAM);
            Long total = jdbcTemplate.queryForObject("""
                    SELECT COUNT(hd.id)
                    FROM orders hd
                    JOIN order_item hdct ON hdct.order_id = hd.id
                    WHERE (? IS NULL OR ? = '' OR LOWER(hd.customer_id) LIKE LOWER(?))
                      AND (? IS NULL OR hd.order_status = ?)
                      AND hd.order_type = ?
                      AND hd.order_status != ?
                    """, Long.class, q, q, q, status, status, ONLINE, LUU_TAM);
            return new ResponseObject<>(Map.of("page", enrichOrderRows(page), "totalRecords", total == null ? 0 : total, "countByStatus", countOnlineByStatus(q)), HttpStatus.OK, "Lay danh sach lich su don hang thanh cong");
        } catch (Exception e) {
            return new ResponseObject<>(null, HttpStatus.INTERNAL_SERVER_ERROR, "Loi khi lay danh sach don hang: " + e.getMessage());
        }
    }

    @Override
    public ResponseObject<?> getAllOrderByCode(String code) {
        List<Map<String, Object>> page = jdbcTemplate.queryForList("""
                SELECT hd.id AS id,
                       hd.code AS maOrder,
                       hdct.product_variant_id AS idSPCT,
                       hdct.quantity AS quantity,
                       hdct.sale_price AS salePrice,
                       hd.order_status AS status,
                       hd.total_after_discount AS tongTien
                FROM orders hd
                JOIN order_item hdct ON hdct.order_id = hd.id
                WHERE hd.code = ?
                  AND hd.order_status != ?
                ORDER BY hd.created_date, hd.code ASC
                """, code, LUU_TAM);
        return new ResponseObject<>(Map.of("page", enrichOrderRows(page), "totalRecords", page.size(), "countByStatus", countByCode(code)), HttpStatus.OK, "Lay danh sach lich su don hang thanh cong");
    }

    @Override
    public ResponseObject<?> getOrderItem(OrderDetailRequest request) {
        if (request.getMaOrder() == null || request.getMaOrder().isBlank()) {
            return new ResponseObject<>(List.of(), HttpStatus.BAD_REQUEST, "Ma hoa don khong duoc de trong");
        }
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT hd.id AS idOrder,
                       hd.code AS maOrder,
                       hd.code AS tenOrder,
                       hdct.code AS maOrderItem,
                       hdct.product_variant_id AS idSPCT,
                       hdct.quantity AS quantity,
                       hdct.sale_price AS salePrice,
                       (hdct.sale_price * hdct.quantity) AS thanhTienSP,
                       (SELECT SUM(hdsub.quantity * hdsub.sale_price) FROM order_item hdsub WHERE hdsub.order_id = hd.id) AS thanhTien,
                       hd.customer_name AS tenCustomer,
                       hd.customer_phone AS sdtKH,
                       hd.email AS email,
                       hd.shipping_address AS address,
                       hd.order_type AS loaiOrder,
                       hd.order_status AS trangThaiOrder,
                       hd.created_date AS ngayTao,
                       hd.shipping_fee AS phiVanCdistrict,
                       hd.voucher_id AS maVoucher,
                       hd.voucher_id AS tenVoucher,
                       hd.discount_amount AS giaTriVoucher,
                       hd.total_after_discount AS tongTienSauGiam,
                       hd.total_after_discount AS tongTien,
                       hd.payment_method AS phuongThucThanhToan,
                       hd.debt_amount AS duNo,
                       hd.refund_amount AS hoanPhi
                FROM order_item hdct
                JOIN orders hd ON hdct.order_id = hd.id
                WHERE hd.code = ?
                  AND hd.order_type = ?
                  AND hd.order_status != ?
                ORDER BY hdct.created_date ASC
                """, request.getMaOrder(), ONLINE, LUU_TAM);
        return new ResponseObject<>(enrichOrderDetailRows(rows), HttpStatus.OK, "Lay danh sach chi tiet hoa don thanh cong");
    }

    @Override
    public List<Map<String, Object>> getCustomerOrderHistory(String customerId) {
        return jdbcTemplate.queryForList("""
                SELECT hd.id AS id,
                       hd.code AS code,
                       hd.name AS name,
                       hd.customer_phone AS phoneNumber,
                       hd.customer_name AS tenKH,
                       hd.shipping_fee AS phiVanCdistrict,
                       hd.shipping_address AS address,
                       hd.total_after_discount AS tongTienSauGiam,
                       hd.total_amount AS tongTien,
                       hd.note AS ghiChu,
                       hd.payment_method AS phuongThucThanhToan,
                       hd.order_type AS loaiOrder,
                       hd.order_status AS trangThaiOrder,
                       hd.created_date AS ngayTao
                FROM orders hd
                WHERE hd.customer_id = ?
                  AND hd.order_type = ?
                ORDER BY hd.created_date DESC
                """, customerId, ONLINE);
    }

    @Override
    public List<Map<String, Object>> getGroupedCustomerOrderHistory(String customerId) {
        List<Map<String, Object>> orders = jdbcTemplate.queryForList("""
                SELECT o.id, o.code, o.customer_name, o.customer_phone, o.shipping_address,
                       o.total_amount, o.total_after_discount, o.shipping_fee, o.payment_method,
                       o.order_status, o.created_date
                FROM orders o
                WHERE o.customer_id = ? AND o.order_type = ? AND o.order_status != ?
                ORDER BY o.created_date DESC
                """, customerId, ONLINE, LUU_TAM);
        return orders.stream().map(order -> {
            Map<String, Object> parent = new LinkedHashMap<>(order);
            List<Map<String, Object>> subOrders = jdbcTemplate.queryForList("""
                    SELECT os.id, os.seller_id, os.shop_name, os.seller_slug, os.total_amount,
                           os.shipping_fee, os.discount_amount, os.total_after_discount,
                           os.order_status, os.created_date
                    FROM order_seller os
                    WHERE os.order_id = ?
                    ORDER BY os.created_date ASC
                    """, order.get("id"));
            parent.put("subOrders", subOrders.stream().map(subOrder -> {
                Map<String, Object> shopOrder = new LinkedHashMap<>(subOrder);
                List<Map<String, Object>> items = jdbcTemplate.queryForList("""
                        SELECT oi.id, oi.product_variant_id, oi.quantity, oi.sale_price,
                               oi.order_seller_id, oi.seller_id
                        FROM order_item oi
                        WHERE oi.order_seller_id = ?
                        ORDER BY oi.created_date ASC
                        """, subOrder.get("id"));
                shopOrder.put("items", items.stream().map(item -> {
                    Map<String, Object> enriched = new LinkedHashMap<>(item);
                    Object variantId = item.get("product_variant_id");
                    CatalogVariantSnapshot product = variantId == null ? null : safeProductDetail(String.valueOf(variantId));
                    enriched.put("productId", product == null ? null : product.productId());
                    enriched.put("productName", product == null ? variantId : product.productName());
                    enriched.put("imageUrl", product == null ? null : product.imageUrl());
                    enriched.put("variantLabel", product == null ? null : product.variantLabel());
                    enriched.put("selections", product == null ? List.of() : product.selections());
                    return enriched;
                }).toList());
                return shopOrder;
            }).toList());
            return parent;
        }).toList();
    }

    @Override
    public Map<String, Object> reviewEligibility(String customerId, String orderSellerId, String productDetailId) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT os.id AS order_seller_id, os.seller_id, oi.product_variant_id
                FROM order_seller os
                JOIN orders o ON o.id = os.order_id
                JOIN order_item oi ON oi.order_seller_id = os.id
                WHERE o.customer_id = ? AND os.id = ? AND oi.product_variant_id = ?
                  AND os.order_status = ?
                LIMIT 1
                """, customerId, orderSellerId, productDetailId, OrderStatusConstant.HOAN_THANH.ordinal());
        if (rows.isEmpty()) {
            return Map.of("eligible", false);
        }
        Map<String, Object> result = new LinkedHashMap<>(rows.get(0));
        result.put("eligible", true);
        return result;
    }

    @Override
    public boolean customerOwnsOrder(String customerId, String orderReference) {
        if (customerId == null || customerId.isBlank() || orderReference == null || orderReference.isBlank()) {
            return false;
        }
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM orders
                WHERE customer_id = ?
                  AND (id = ? OR code = ?)
                """, Integer.class, customerId, orderReference, orderReference);
        return count != null && count > 0;
    }

    @Override
    public ResponseObject<?> getAllProductVariant(ProductVariantSearchRequest request) {
        List<Map<String, Object>> rows = catalogClient.searchProductVariants(
                request.getQ(), stringValue(request.getEntityStatus()), request.getIdSP(),
                decimal(request.getPriceMin()), decimal(request.getPriceMax())).stream()
                .map(DonMuaServiceImpl::variantMap)
                .toList();
        int offset = Math.max(request.getPage() - 1, 0) * pageSize(request);
        for (int i = 0; i < rows.size(); i++) {
            rows.get(i).put("stt", i + 1);
        }
        rows = slice(rows, offset, pageSize(request));
        return new ResponseObject<>(rows, HttpStatus.OK, "Lay danh sach san pham chi tiet thanh cong");
    }

    @Override
    @Transactional
    public ResponseObject<?> suaThongTin(UpdateDeliveryRequest request) {
        Map<String, Object> hoaDon = jdbcTemplate.queryForMap("SELECT id, order_status, total_after_discount, debt_amount, refund_amount FROM orders WHERE code = ?", request.getMaOrder());
        if (((Number) hoaDon.get("order_status")).intValue() != CHO_XAC_NHAN) {
            return new ResponseObject<>(null, HttpStatus.BAD_REQUEST, "Chi co the cap nhat thong tin giao hang khi don hang dang cho wardc nhan");
        }
        double oldTotal = doubleValue(hoaDon.get("total_after_discount"));
        double newTotal = doubleValue(request.getTongTienSauGiam());
        double duNo = doubleValue(hoaDon.get("debt_amount"));
        double hoanPhi = doubleValue(hoaDon.get("refund_amount"));
        if (oldTotal > newTotal) {
            if (hoaDon.get("debt_amount") != null) {
                duNo = duNo + oldTotal - newTotal;
                if (hoaDon.get("refund_amount") != null && hoanPhi > 0) {
                    if (hoanPhi - (newTotal - oldTotal) <= 0) {
                        duNo = 0D;
                    } else {
                        duNo = duNo - (oldTotal - newTotal);
                        hoanPhi = 0D;
                    }
                }
            } else {
                hoanPhi = newTotal - oldTotal;
            }
        } else if (oldTotal < newTotal) {
            if (hoaDon.get("refund_amount") != null) {
                hoanPhi = hoanPhi + newTotal - oldTotal;
                if (hoaDon.get("debt_amount") != null && duNo > 0) {
                    if (duNo - (oldTotal - newTotal) <= 0) {
                        duNo = 0D;
                    } else {
                        duNo = 0D;
                    }
                }
            } else {
                duNo = newTotal - oldTotal;
            }
        }
        jdbcTemplate.update("""
                UPDATE orders
                SET customer_name = ?, customer_phone = ?, email = ?, shipping_address = ?,
                    shipping_fee = ?, total_after_discount = ?, debt_amount = ?, refund_amount = ?
                WHERE code = ?
                """, request.getTenCustomer(), request.getSdtCustomer(), request.getEmail(), request.getAddress(),
                request.getPhiVanCdistrict(), newTotal, duNo, hoanPhi, request.getMaOrder());
        return new ResponseObject<>(Map.of("maOrder", request.getMaOrder()), HttpStatus.OK, "Lay danh sach lich su don hang thanh cong");
    }

    @Override
    @Transactional
    public ResponseObject<?> cancelOrder(ChangeStatusRequest request) {
        Map<String, Object> order = jdbcTemplate.queryForMap(
                "SELECT id, order_status, voucher_id FROM orders WHERE code = ? FOR UPDATE", request.getMaOrder());
        String orderId = String.valueOf(order.get("id"));
        if (intValue(order.get("order_status")) != CHO_XAC_NHAN) {
            return new ResponseObject<>(null, HttpStatus.BAD_REQUEST,
                    "Chi co the huy don hang dang cho xac nhan");
        }

        Integer progressedSubOrders = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM order_seller
                WHERE order_id = ? AND order_status <> ?
                """, Integer.class, orderId, CHO_XAC_NHAN);
        if (progressedSubOrders != null && progressedSubOrders > 0) {
            return new ResponseObject<>(null, HttpStatus.BAD_REQUEST,
                    "Khong the huy don vi mot shop da xu ly don hang");
        }

        List<Map<String, Object>> items = jdbcTemplate.queryForList("""
                SELECT product_variant_id, quantity
                FROM order_item
                WHERE order_id = ?
                """, orderId);
        for (Map<String, Object> item : items) {
            catalogClient.adjustStock(String.valueOf(item.get("product_variant_id")), intValue(item.get("quantity")));
        }
        if (order.get("voucher_id") != null) {
            promotionClient.incrementVoucher(String.valueOf(order.get("voucher_id")));
        }

        jdbcTemplate.update("UPDATE orders SET order_status = ? WHERE id = ?", DA_HUY, orderId);
        jdbcTemplate.update("UPDATE order_seller SET order_status = ? WHERE order_id = ?", DA_HUY, orderId);
        jdbcTemplate.update("""
                INSERT INTO order_status_history (order_id, status, payment_time, note)
                VALUES (?, ?, ?, ?)
                """, orderId, DA_HUY, LocalDateTime.now(), request.getNote());
        return new ResponseObject<>(Map.of("maOrder", request.getMaOrder()), HttpStatus.OK,
                "Huy don hang thanh cong");
    }

    @Override
    public ResponseObject<?> getOrderStatusHistory(String orderId) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT status AS trangThai, payment_time AS thoiGian, note AS note
                FROM order_status_history
                WHERE order_id = ?
                ORDER BY payment_time DESC
                """, orderId);
        return new ResponseObject<>(rows, HttpStatus.OK, "Lay lich su trang thai don hang thanh cong");
    }

    @Override
    public ResponseObject<?> getPaymentHistory(String orderId) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT ROW_NUMBER() OVER (ORDER BY payment_time DESC) AS stt,
                       amount AS soTien,
                       payment_time AS thoiGian,
                       transaction_code AS maGiaoDich,
                       transaction_type AS loaiGiaoDich,
                       note AS ghiChu,
                       staff_id AS tenStaff,
                       order_id AS hoaDonId
                FROM payment_history
                WHERE order_id = ?
                ORDER BY payment_time DESC
                """, orderId);
        return new ResponseObject<>(rows, HttpStatus.OK, "Lay lich su thanh toan don hang thanh cong");
    }

    @Override
    @Transactional
    public ResponseObject<?> themProduct(ThemProductRequest request) {
        CatalogVariantSnapshot product = catalogClient.getProductVariant(request.getIdSP());
        List<Map<String, Object>> existing = jdbcTemplate.queryForList("""
                SELECT id, quantity, sale_price
                FROM order_item
                WHERE order_id = ? AND product_variant_id = ?
                ORDER BY created_date DESC
                """, request.getIdHD(), request.getIdSP());
        int stock = product.quantity();
        double currentPrice = product.salePrice().doubleValue();
        if (existing.isEmpty()) {
            if (stock < 1) {
                return new ResponseObject<>(null, HttpStatus.OK, "So luong san pham them vao nhieu hon so luong trong kho");
            }
            insertOrderItem(request.getIdHD(), request.getIdSP(), currentPrice, 1);
            return new ResponseObject<>(null, HttpStatus.OK, "them san pham thanh cong");
        }

        Map<String, Object> detail = existing.get(0);
        double oldPrice = doubleValue(detail.get("sale_price"));
        if (Math.abs(oldPrice - currentPrice) > 0.0001D) {
            if (stock < 1) {
                return new ResponseObject<>(null, HttpStatus.OK, "So luong san pham them vao nhieu hon so luong trong kho");
            }
            insertOrderItem(request.getIdHD(), request.getIdSP(), currentPrice, 1);
            return new ResponseObject<>(null, HttpStatus.OK, "San pham nay dang duoc thay doi gia tu " + oldPrice + "d thanh " + currentPrice);
        }
        int nextQuantity = intValue(detail.get("quantity")) + 1;
        if (stock < nextQuantity) {
            return new ResponseObject<>(null, HttpStatus.OK, "So luong san pham them vao nhieu hon so luong trong kho");
        }
        jdbcTemplate.update("UPDATE order_item SET quantity = ? WHERE id = ?", nextQuantity, detail.get("id"));
        return new ResponseObject<>(null, HttpStatus.OK, "them san pham");
    }

    private Map<OrderStatusConstant, Long> countOnlineByStatus(String q) {
        Map<OrderStatusConstant, Long> result = new LinkedHashMap<>();
        jdbcTemplate.query("""
                SELECT hd.order_status, COUNT(hd.id) AS total
                FROM orders hd
                WHERE (? IS NULL OR ? = '' OR LOWER(hd.customer_id) LIKE LOWER(?))
                  AND hd.order_type = ?
                  AND hd.order_status != ?
                GROUP BY hd.order_status
                """, (RowCallbackHandler) rs ->
                result.put(OrderStatusConstant.values()[rs.getInt("order_status")], rs.getLong("total")), q, q, q, ONLINE, LUU_TAM);
        return result;
    }

    private Map<OrderStatusConstant, Long> countByCode(String code) {
        Map<OrderStatusConstant, Long> result = new LinkedHashMap<>();
        jdbcTemplate.query("""
                SELECT hd.order_status, COUNT(hd.id) AS total
                FROM orders hd
                WHERE hd.code = ?
                  AND hd.order_status != ?
                GROUP BY hd.order_status
                """, (RowCallbackHandler) rs ->
                result.put(OrderStatusConstant.values()[rs.getInt("order_status")], rs.getLong("total")), code, LUU_TAM);
        return result;
    }

    private void insertOrderItem(String hoaDonId, String productVariantId, double price, int quantity) {
        jdbcTemplate.update("""
                INSERT INTO order_item (id, status, created_date, code, quantity, sale_price, product_variant_id, order_id)
                VALUES (?, 0, ?, ?, ?, ?, ?, ?)
                """, UUID.randomUUID().toString(), System.currentTimeMillis(), generateCodeOrderItem(), quantity, price, productVariantId, hoaDonId);
    }

    private List<Map<String, Object>> enrichOrderRows(List<Map<String, Object>> rows) {
        return rows.stream().map(row -> {
            Map<String, Object> enriched = new LinkedHashMap<>(row);
            Object productDetailId = row.get("idSPCT");
            if (productDetailId == null) {
                productDetailId = row.get("idspct");
            }
            if (productDetailId != null) {
                CatalogVariantSnapshot product = catalogClient.getProductVariant(String.valueOf(productDetailId));
                enriched.put("imageUrl", product.imageUrl());
                enriched.put("tenProduct", product.productName());
                enriched.put("variantLabel", product.variantLabel());
                enriched.put("selections", product.selections());
            }
            return enriched;
        }).toList();
    }

    private List<Map<String, Object>> enrichOrderDetailRows(List<Map<String, Object>> rows) {
        return rows.stream().map(row -> {
            Map<String, Object> enriched = new LinkedHashMap<>(row);
            Object productDetailId = row.get("idSPCT");
            if (productDetailId == null) {
                productDetailId = row.get("idspct");
            }
            CatalogVariantSnapshot product = productDetailId == null ? null : safeProductDetail(String.valueOf(productDetailId));
            enriched.put("tenProduct", product == null ? productDetailId : product.productName());
            enriched.put("anhProduct", product == null ? null : product.imageUrl());
            enriched.put("variantLabel", product == null ? null : product.variantLabel());
            enriched.put("selections", product == null ? List.of() : product.selections());
            return enriched;
        }).toList();
    }

    private CatalogVariantSnapshot safeProductDetail(String productDetailId) {
        try {
            return catalogClient.getProductVariant(productDetailId);
        } catch (Exception e) {
            return null;
        }
    }

    private static Map<String, Object> variantMap(CatalogVariantSnapshot snapshot) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", snapshot.id());
        row.put("productId", snapshot.productId());
        row.put("sellerId", snapshot.sellerId());
        row.put("sku", snapshot.sku());
        row.put("productName", snapshot.productName());
        row.put("variantLabel", snapshot.variantLabel());
        row.put("selections", snapshot.selections());
        row.put("salePrice", snapshot.salePrice());
        row.put("quantity", snapshot.quantity());
        row.put("imageUrl", snapshot.imageUrl());
        row.put("status", snapshot.status());
        return row;
    }

    private static BigDecimal decimal(Double value) {
        return value == null ? null : BigDecimal.valueOf(value);
    }

    private static Object firstNonNull(Object... values) {
        for (Object value : values) {
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private static List<Map<String, Object>> slice(List<Map<String, Object>> rows, int offset, int size) {
        if (offset >= rows.size()) {
            return List.of();
        }
        return rows.subList(offset, Math.min(rows.size(), offset + size));
    }

    private static String like(String q) {
        return q == null || q.trim().isEmpty() ? "" : "%" + q.trim() + "%";
    }

    private static int intValue(Object value) {
        return value == null ? 0 : ((Number) value).intValue();
    }

    private static double doubleValue(Object value) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        return value == null ? 0D : Double.parseDouble(String.valueOf(value));
    }

    private static int pageSize(ProductVariantSearchRequest request) {
        return request.getSize() <= 0 ? 10 : request.getSize();
    }

    private static String stringValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Enum<?> enumValue) {
            return String.valueOf(enumValue.ordinal());
        }
        return String.valueOf(value);
    }

    private static String generateCodeOrderItem() {
        return "HDCT" + String.format("%04d", ThreadLocalRandom.current().nextInt(10000));
    }
}
