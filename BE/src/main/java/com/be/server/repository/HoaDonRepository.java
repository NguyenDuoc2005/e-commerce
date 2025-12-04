package com.be.server.repository;

import com.be.server.entity.HoaDon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HoaDonRepository extends JpaRepository<HoaDon, String> {

    // Doanh số tháng này (trạng thái 4 = HOAN_THANH)
    @Query(value = """
            SELECT COALESCE(SUM(h.tong_tien_sau_giam), 0) 
            FROM hoa_don h 
            WHERE h.trang_thai_hoa_don = 4
            AND h.created_date >= :startOfMonth 
            AND h.created_date <= :endOfMonth
            """, nativeQuery = true)
    Double getDoanhSoThangNay(@Param("startOfMonth") Long startOfMonth,
                              @Param("endOfMonth") Long endOfMonth);

    // Số hóa đơn tháng này (trạng thái 4 = HOAN_THANH)
    @Query(value = """
            SELECT COUNT(h.id) 
            FROM hoa_don h 
            WHERE h.trang_thai_hoa_don = 4
            AND h.created_date >= :startOfMonth 
            AND h.created_date <= :endOfMonth
            """, nativeQuery = true)
    Integer getSoHoaDonThangNay(@Param("startOfMonth") Long startOfMonth,
                                @Param("endOfMonth") Long endOfMonth);

    // Doanh số hôm nay (trạng thái 4 = HOAN_THANH)
    @Query(value = """
            SELECT COALESCE(SUM(h.tong_tien_sau_giam), 0) 
            FROM hoa_don h 
            WHERE h.trang_thai_hoa_don = 4
            AND h.created_date >= :startOfDay 
            AND h.created_date <= :endOfDay
            """, nativeQuery = true)
    Double getDoanhSoHomNay(@Param("startOfDay") Long startOfDay,
                            @Param("endOfDay") Long endOfDay);

    // Số hóa đơn hôm nay (trạng thái 4 = HOAN_THANH)
    @Query(value = """
            SELECT COUNT(h.id) 
            FROM hoa_don h 
            WHERE h.trang_thai_hoa_don = 4
            AND h.created_date >= :startOfDay 
            AND h.created_date <= :endOfDay
            """, nativeQuery = true)
    Integer getSoHoaDonHomNay(@Param("startOfDay") Long startOfDay,
                              @Param("endOfDay") Long endOfDay);

    // Số lượng sản phẩm bán được tháng này (trạng thái 4 = HOAN_THANH)
    @Query(value = """
            SELECT COALESCE(SUM(hd.so_luong), 0) 
            FROM hoa_don_chi_tiet hd 
            INNER JOIN hoa_don h ON hd.id_hoa_don = h.id 
            WHERE h.trang_thai_hoa_don = 4
            AND h.created_date >= :startOfMonth 
            AND h.created_date <= :endOfMonth
            """, nativeQuery = true)
    Integer getHangBanDuocThangNay(@Param("startOfMonth") Long startOfMonth,
                                   @Param("endOfMonth") Long endOfMonth);

    // Query test để kiểm tra dữ liệu
    @Query(value = """
            SELECT h.id, h.tong_tien_sau_giam, h.created_date, h.trang_thai_hoa_don 
            FROM hoa_don h 
            ORDER BY h.created_date DESC 
            LIMIT 10
            """, nativeQuery = true)
    List<Object[]> getTestData();

    // Đếm tổng số hóa đơn (không filter gì)
    @Query(value = "SELECT COUNT(*) FROM hoa_don", nativeQuery = true)
    Integer getTotalHoaDonCount();

    @Query(value = """
            SELECT\s
                DATE_FORMAT(FROM_UNIXTIME(h.created_date / 1000), '%d/%m/%Y') as date,
                COUNT(h.id) as soLuongDonHang
            FROM hoa_don h
            WHERE h.trang_thai_hoa_don = '4'
              AND h.created_date >= :startDate\s
              AND h.created_date <= :endDate
            GROUP BY date
            ORDER BY date ASC
            """, nativeQuery = true)
    List<Object[]> thongKeDonHangHoanThanhTheoNgay(@Param("startDate") Long startDate,
                                                   @Param("endDate") Long endDate);

    @Query(value = """
            SELECT 
                sp.id,
                sp.ma_san_pham,
                sp.ten_san_pham,
                spct.anh_san_pham,
                SUM(hdct.so_luong) as soLuongBan,
                SUM(hdct.so_luong * spct.gia_ban) as doanhThu,
                th.ten_thuong_hieu,
                spct.gia_ban as giaBan
            FROM hoa_don_chi_tiet hdct
            INNER JOIN san_pham_chi_tiet spct ON hdct.id_spct = spct.id
            INNER JOIN san_pham sp ON spct.id_san_pham = sp.id
            INNER JOIN hoa_don h ON hdct.id_hoa_don = h.id
            LEFT JOIN thuong_hieu th ON sp.id_thuong_hieu = th.id
            WHERE h.trang_thai_hoa_don = '4'
            AND (:startDate IS NULL OR h.created_date >= :startDate)
            AND (:endDate IS NULL OR h.created_date <= :endDate)
            GROUP BY sp.id, sp.ma_san_pham, sp.ten_san_pham, spct.anh_san_pham, th.ten_thuong_hieu, spct.gia_ban
            ORDER BY SUM(hdct.so_luong) DESC
            LIMIT 3
            """, nativeQuery = true)
    List<Object[]> layTop3SanPhamBanChay(@Param("startDate") Long startDate,
                                         @Param("endDate") Long endDate);

    @Query(value = """
            SELECT 
                h.trang_thai_hoa_don as trangThai,
                COUNT(h.id) as soLuong
            FROM hoa_don h
            WHERE h.created_date >= :startDate
              AND h.created_date <= :endDate
            GROUP BY h.trang_thai_hoa_don
            """, nativeQuery = true)
    List<Object[]> countHoaDonByTrangThaiInPeriod(
            @Param("startDate") Long startDate,
            @Param("endDate") Long endDate
    );

    @Query(value = """
            SELECT COUNT(h.id)
            FROM hoa_don h
            WHERE h.created_date >= :startDate
              AND h.created_date <= :endDate
            """, nativeQuery = true)
    long countTotalHoaDonInPeriod(
            @Param("startDate") Long startDate,
            @Param("endDate") Long endDate
    );

    Optional<HoaDon> findByMa(String ma);

}