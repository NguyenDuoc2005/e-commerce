package com.ecommerce.order.service;

import com.ecommerce.order.constant.OrderStatusConstant;
import com.ecommerce.order.client.CatalogClient;
import com.ecommerce.order.model.response.ThongKeDoanhThuResponse;
import com.ecommerce.order.model.response.ThongKeDonHangResponse;
import com.ecommerce.order.model.response.OrderStatusStatisticsResponse;
import com.ecommerce.order.model.response.TopSellingProductResponse;
import com.ecommerce.order.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ThongKeDoanhThuService {
    private final OrderRepository hoaDonRepository;
    private final CatalogClient catalogClient;
    private final JdbcTemplate jdbcTemplate;

    public ThongKeDoanhThuService(OrderRepository hoaDonRepository, CatalogClient catalogClient, JdbcTemplate jdbcTemplate) {
        this.hoaDonRepository = hoaDonRepository;
        this.catalogClient = catalogClient;
        this.jdbcTemplate = jdbcTemplate;
    }

    public ThongKeDoanhThuResponse getThongKeDoanhThu() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfMonth = now.withDayOfMonth(now.toLocalDate().lengthOfMonth()).withHour(23).withMinute(59).withSecond(59).withNano(999999999);
        LocalDateTime startOfDay = now.withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfDay = now.withHour(23).withMinute(59).withSecond(59).withNano(999999999);
        ZoneOffset zone = ZoneOffset.ofHours(7);
        Long startOfMonthTs = startOfMonth.toEpochSecond(zone) * 1000;
        Long endOfMonthTs = endOfMonth.toEpochSecond(zone) * 1000;
        Long startOfDayTs = startOfDay.toEpochSecond(zone) * 1000;
        Long endOfDayTs = endOfDay.toEpochSecond(zone) * 1000;
        return new ThongKeDoanhThuResponse(
                defaultDouble(hoaDonRepository.getDoanhSoThangNay(startOfMonthTs, endOfMonthTs)),
                defaultInteger(hoaDonRepository.getSoOrderThangNay(startOfMonthTs, endOfMonthTs)),
                defaultDouble(hoaDonRepository.getDoanhSoHomNay(startOfDayTs, endOfDayTs)),
                defaultInteger(hoaDonRepository.getSoOrderHomNay(startOfDayTs, endOfDayTs)),
                defaultInteger(hoaDonRepository.getHangBanDuocThangNay(startOfMonthTs, endOfMonthTs))
        );
    }

    public List<ThongKeDonHangResponse> thongKeDonHangHoanThanhTheoKhoangThoiGian(Long startDate, Long endDate) {
        Map<String, Long> data = hoaDonRepository.thongKeDonHangHoanThanhTheoNgay(startDate, endDate).stream()
                .collect(Collectors.toMap(row -> (String) row[0], row -> ((Number) row[1]).longValue()));
        List<ThongKeDonHangResponse> result = new ArrayList<>();
        LocalDate start = java.time.Instant.ofEpochMilli(startDate).atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        LocalDate end = java.time.Instant.ofEpochMilli(endDate).atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        for (LocalDate current = start; !current.isAfter(end); current = current.plusDays(1)) {
            String formatted = current.format(formatter);
            result.add(new ThongKeDonHangResponse(formatted, data.getOrDefault(formatted, 0L)));
        }
        return result;
    }

    public List<TopSellingProductResponse> layTop3ProductBanChay(Long startDate, Long endDate) {
        List<TopSellingProductResponse> result = new ArrayList<>();
        for (Object[] row : hoaDonRepository.layTop3ProductBanChay(startDate, endDate)) {
            Map<String, Object> product = catalogClient.getProductDetail((String) row[0]);
            TopSellingProductResponse response = new TopSellingProductResponse();
            response.setId((String) row[0]);
            response.setMaProduct((String) product.get("code"));
            response.setTenProduct((String) product.get("name"));
            response.setAnhProduct((String) product.get("imageUrl"));
            response.setSoLuongBan(((Number) row[1]).longValue());
            response.setDoanhThu(row[2] == null ? 0.0 : ((Number) row[2]).doubleValue());
            response.setBrand((String) product.get("tenBrand"));
            response.setSalePrice(row[3] == null ? 0.0 : ((Number) row[3]).doubleValue());
            result.add(response);
        }
        return result;
    }

    public List<OrderStatusStatisticsResponse> thongKeTiLeTrangThaiOrder(Long startDate, Long endDate) {
        long total = hoaDonRepository.countTotalOrderInPeriod(startDate, endDate);
        Map<Integer, Long> statusMap = hoaDonRepository.countOrderByTrangThaiInPeriod(startDate, endDate).stream()
                .collect(Collectors.toMap(row -> ((Number) row[0]).intValue(), row -> ((Number) row[1]).longValue()));
        List<OrderStatusStatisticsResponse> result = new ArrayList<>();
        for (OrderStatusConstant status : OrderStatusConstant.values()) {
            long count = statusMap.getOrDefault(status.ordinal(), 0L);
            result.add(new OrderStatusStatisticsResponse(status.name(), count, total > 0 ? (count * 100.0 / total) : 0));
        }
        return result;
    }

    public Map<String, Object> marketplaceDashboard() {
        Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("gmv", scalarDouble("SELECT COALESCE(SUM(total_after_discount), 0) FROM order_seller WHERE order_status = 4"));
        result.put("totalSubOrders", scalarLong("SELECT COUNT(*) FROM order_seller"));
        result.put("completedSubOrders", scalarLong("SELECT COUNT(*) FROM order_seller WHERE order_status = 4"));
        result.put("activeSellers", scalarLong("SELECT COUNT(DISTINCT seller_id) FROM order_seller"));
        result.put("topSellers", jdbcTemplate.queryForList("""
                SELECT seller_id AS sellerId, MAX(shop_name) AS shopName,
                       COUNT(*) AS orderCount, COALESCE(SUM(total_after_discount), 0) AS revenue
                FROM order_seller WHERE order_status = 4
                GROUP BY seller_id ORDER BY revenue DESC LIMIT 10
                """));
        result.put("topProducts", jdbcTemplate.queryForList("""
                SELECT oi.product_variant_id AS productVariantId, MAX(oi.name) AS productName,
                       SUM(oi.quantity) AS soldCount, SUM(oi.quantity * oi.sale_price) AS revenue
                FROM order_item oi JOIN order_seller os ON os.id = oi.order_seller_id
                WHERE os.order_status = 4
                GROUP BY oi.product_variant_id ORDER BY soldCount DESC LIMIT 10
                """));
        return result;
    }

    private double scalarDouble(String sql) {
        Double value = jdbcTemplate.queryForObject(sql, Double.class);
        return value == null ? 0D : value;
    }

    private long scalarLong(String sql) {
        Long value = jdbcTemplate.queryForObject(sql, Long.class);
        return value == null ? 0L : value;
    }

    private Double defaultDouble(Double value) { return value == null ? 0.0 : value; }
    private Integer defaultInteger(Integer value) { return value == null ? 0 : value; }
}
