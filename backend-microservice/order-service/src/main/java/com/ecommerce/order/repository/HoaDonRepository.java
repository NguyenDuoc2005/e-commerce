package com.ecommerce.order.repository;

import com.ecommerce.order.entity.HoaDon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface HoaDonRepository extends JpaRepository<HoaDon, String> {
    @Query(value = "SELECT COALESCE(SUM(h.tong_tien_sau_giam), 0) FROM hoa_don h WHERE h.trang_thai_hoa_don = 4 AND h.created_date >= :startOfMonth AND h.created_date <= :endOfMonth", nativeQuery = true)
    Double getDoanhSoThangNay(@Param("startOfMonth") Long startOfMonth, @Param("endOfMonth") Long endOfMonth);

    @Query(value = "SELECT COUNT(h.id) FROM hoa_don h WHERE h.trang_thai_hoa_don = 4 AND h.created_date >= :startOfMonth AND h.created_date <= :endOfMonth", nativeQuery = true)
    Integer getSoHoaDonThangNay(@Param("startOfMonth") Long startOfMonth, @Param("endOfMonth") Long endOfMonth);

    @Query(value = "SELECT COALESCE(SUM(h.tong_tien_sau_giam), 0) FROM hoa_don h WHERE h.trang_thai_hoa_don = 4 AND h.created_date >= :startOfDay AND h.created_date <= :endOfDay", nativeQuery = true)
    Double getDoanhSoHomNay(@Param("startOfDay") Long startOfDay, @Param("endOfDay") Long endOfDay);

    @Query(value = "SELECT COUNT(h.id) FROM hoa_don h WHERE h.trang_thai_hoa_don = 4 AND h.created_date >= :startOfDay AND h.created_date <= :endOfDay", nativeQuery = true)
    Integer getSoHoaDonHomNay(@Param("startOfDay") Long startOfDay, @Param("endOfDay") Long endOfDay);

    @Query(value = "SELECT COALESCE(SUM(hd.so_luong), 0) FROM hoa_don_chi_tiet hd INNER JOIN hoa_don h ON hd.id_hoa_don = h.id WHERE h.trang_thai_hoa_don = 4 AND h.created_date >= :startOfMonth AND h.created_date <= :endOfMonth", nativeQuery = true)
    Integer getHangBanDuocThangNay(@Param("startOfMonth") Long startOfMonth, @Param("endOfMonth") Long endOfMonth);

    @Query(value = """
            SELECT DATE_FORMAT(FROM_UNIXTIME(h.created_date / 1000), '%d/%m/%Y') as date, COUNT(h.id) as soLuongDonHang
            FROM hoa_don h
            WHERE h.trang_thai_hoa_don = '4' AND h.created_date >= :startDate AND h.created_date <= :endDate
            GROUP BY date
            ORDER BY date ASC
            """, nativeQuery = true)
    List<Object[]> thongKeDonHangHoanThanhTheoNgay(@Param("startDate") Long startDate, @Param("endDate") Long endDate);

    @Query(value = """
            SELECT hdct.id_spct, SUM(hdct.so_luong) as soLuongBan,
                   SUM(hdct.so_luong * hdct.gia_ban) as doanhThu, MAX(hdct.gia_ban) as giaBan
            FROM hoa_don_chi_tiet hdct
            INNER JOIN hoa_don h ON hdct.id_hoa_don = h.id
            WHERE h.trang_thai_hoa_don = '4'
              AND (:startDate IS NULL OR h.created_date >= :startDate)
              AND (:endDate IS NULL OR h.created_date <= :endDate)
            GROUP BY hdct.id_spct
            ORDER BY SUM(hdct.so_luong) DESC
            LIMIT 3
            """, nativeQuery = true)
    List<Object[]> layTop3SanPhamBanChay(@Param("startDate") Long startDate, @Param("endDate") Long endDate);

    @Query(value = "SELECT h.trang_thai_hoa_don as trangThai, COUNT(h.id) as soLuong FROM hoa_don h WHERE h.created_date >= :startDate AND h.created_date <= :endDate GROUP BY h.trang_thai_hoa_don", nativeQuery = true)
    List<Object[]> countHoaDonByTrangThaiInPeriod(@Param("startDate") Long startDate, @Param("endDate") Long endDate);

    @Query(value = "SELECT COUNT(h.id) FROM hoa_don h WHERE h.created_date >= :startDate AND h.created_date <= :endDate", nativeQuery = true)
    long countTotalHoaDonInPeriod(@Param("startDate") Long startDate, @Param("endDate") Long endDate);
}
