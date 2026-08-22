package com.ecommerce.order.repository;

import com.ecommerce.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, String> {
    @Query(value = "SELECT COALESCE(SUM(h.total_after_discount), 0) FROM orders h WHERE h.order_status = 4 AND h.created_date >= :startOfMonth AND h.created_date <= :endOfMonth", nativeQuery = true)
    Double getDoanhSoThangNay(@Param("startOfMonth") Long startOfMonth, @Param("endOfMonth") Long endOfMonth);

    @Query(value = "SELECT COUNT(h.id) FROM orders h WHERE h.order_status = 4 AND h.created_date >= :startOfMonth AND h.created_date <= :endOfMonth", nativeQuery = true)
    Integer getSoOrderThangNay(@Param("startOfMonth") Long startOfMonth, @Param("endOfMonth") Long endOfMonth);

    @Query(value = "SELECT COALESCE(SUM(h.total_after_discount), 0) FROM orders h WHERE h.order_status = 4 AND h.created_date >= :startOfDay AND h.created_date <= :endOfDay", nativeQuery = true)
    Double getDoanhSoHomNay(@Param("startOfDay") Long startOfDay, @Param("endOfDay") Long endOfDay);

    @Query(value = "SELECT COUNT(h.id) FROM orders h WHERE h.order_status = 4 AND h.created_date >= :startOfDay AND h.created_date <= :endOfDay", nativeQuery = true)
    Integer getSoOrderHomNay(@Param("startOfDay") Long startOfDay, @Param("endOfDay") Long endOfDay);

    @Query(value = "SELECT COALESCE(SUM(hd.quantity), 0) FROM order_item hd INNER JOIN orders h ON hd.order_id = h.id WHERE h.order_status = 4 AND h.created_date >= :startOfMonth AND h.created_date <= :endOfMonth", nativeQuery = true)
    Integer getHangBanDuocThangNay(@Param("startOfMonth") Long startOfMonth, @Param("endOfMonth") Long endOfMonth);

    @Query(value = """
            SELECT DATE_FORMAT(FROM_UNIXTIME(h.created_date / 1000), '%d/%m/%Y') as date, COUNT(h.id) as soLuongDonHang
            FROM orders h
            WHERE h.order_status = '4' AND h.created_date >= :startDate AND h.created_date <= :endDate
            GROUP BY date
            ORDER BY date ASC
            """, nativeQuery = true)
    List<Object[]> thongKeDonHangHoanThanhTheoNgay(@Param("startDate") Long startDate, @Param("endDate") Long endDate);

    @Query(value = """
            SELECT hdct.product_variant_id, SUM(hdct.quantity) as soLuongBan,
                   SUM(hdct.quantity * hdct.sale_price) as doanhThu, MAX(hdct.sale_price) as salePrice
            FROM order_item hdct
            INNER JOIN orders h ON hdct.order_id = h.id
            WHERE h.order_status = '4'
              AND (:startDate IS NULL OR h.created_date >= :startDate)
              AND (:endDate IS NULL OR h.created_date <= :endDate)
            GROUP BY hdct.product_variant_id
            ORDER BY SUM(hdct.quantity) DESC
            LIMIT 3
            """, nativeQuery = true)
    List<Object[]> layTop3ProductBanChay(@Param("startDate") Long startDate, @Param("endDate") Long endDate);

    @Query(value = "SELECT h.order_status as trangThai, COUNT(h.id) as quantity FROM orders h WHERE h.created_date >= :startDate AND h.created_date <= :endDate GROUP BY h.order_status", nativeQuery = true)
    List<Object[]> countOrderByTrangThaiInPeriod(@Param("startDate") Long startDate, @Param("endDate") Long endDate);

    @Query(value = "SELECT COUNT(h.id) FROM orders h WHERE h.created_date >= :startDate AND h.created_date <= :endDate", nativeQuery = true)
    long countTotalOrderInPeriod(@Param("startDate") Long startDate, @Param("endDate") Long endDate);
}
