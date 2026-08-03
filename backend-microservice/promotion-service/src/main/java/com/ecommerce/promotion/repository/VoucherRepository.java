package com.ecommerce.promotion.repository;

import com.ecommerce.promotion.entity.PhieuGiamGia;
import com.ecommerce.promotion.model.request.VoucherSearchRequest;
import com.ecommerce.promotion.model.response.VoucherResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface VoucherRepository extends JpaRepository<PhieuGiamGia, String> {

    @Query(value = """
            SELECT
                pgg.id AS id,
                pgg.ma AS ma,
                pgg.ten AS ten,
                pgg.dieuKien AS dieuKien,
                pgg.giaGiam AS giaGiam,
                pgg.kieuGiam AS kieuGiam,
                pgg.loaiGiam AS loaiGiam,
                pgg.phanTramGiam AS phanTramGiam,
                pgg.soLuongPhieu AS soLuongPhieu,
                pgg.ngayBatDau AS ngayBatDau,
                pgg.ngayKetThuc AS ngayKetThuc,
                pgg.status AS status
            FROM PhieuGiamGia pgg
            WHERE (:#{#req.q == null || #req.q.isEmpty()} = TRUE OR LOWER(pgg.ma) LIKE LOWER(CONCAT('%', :#{#req.q}, '%')) OR LOWER(pgg.ten) LIKE LOWER(CONCAT('%', :#{#req.q}, '%')))
              AND (:#{#req.startDate == null} = TRUE OR pgg.ngayBatDau >= :#{#req.startDate})
              AND (:#{#req.endDate == null} = TRUE OR pgg.ngayKetThuc <= :#{#req.endDate})
              AND (:#{#req.kieuGiam == null} = TRUE OR pgg.kieuGiam = :#{#req.kieu})
              AND (:#{#req.status == null} = TRUE OR pgg.status = :#{#req.entityStatus})
            ORDER BY pgg.createdDate DESC
            """, countQuery = """
            SELECT COUNT(pgg.id)
            FROM PhieuGiamGia pgg
            WHERE (:#{#req.q == null || #req.q.isEmpty()} = TRUE OR LOWER(pgg.ma) LIKE LOWER(CONCAT('%', :#{#req.q}, '%')) OR LOWER(pgg.ten) LIKE LOWER(CONCAT('%', :#{#req.q}, '%')))
              AND (:#{#req.startDate == null} = TRUE OR pgg.ngayBatDau >= :#{#req.startDate})
              AND (:#{#req.endDate == null} = TRUE OR pgg.ngayKetThuc <= :#{#req.endDate})
              AND (:#{#req.kieuGiam == null} = TRUE OR pgg.kieuGiam = :#{#req.kieu})
              AND (:#{#req.status == null} = TRUE OR pgg.status = :#{#req.entityStatus})
            """)
    Page<VoucherResponse> getAllPhieuGiamGiaFilter(Pageable pageable, @Param("req") VoucherSearchRequest req);

    @Query("""
            SELECT
                pgg.id AS id,
                pgg.ma AS ma,
                pgg.ten AS ten,
                pgg.dieuKien AS dieuKien,
                pgg.giaGiam AS giaGiam,
                pgg.kieuGiam AS kieuGiam,
                pgg.loaiGiam AS loaiGiam,
                pgg.phanTramGiam AS phanTramGiam,
                pgg.soLuongPhieu AS soLuongPhieu,
                pgg.ngayBatDau AS ngayBatDau,
                pgg.ngayKetThuc AS ngayKetThuc,
                pgg.status AS status
            FROM PhieuGiamGia pgg
            WHERE pgg.id LIKE CONCAT('%', :id, '%')
            """)
    Optional<VoucherResponse> getVoucherById(@Param("id") String id);

    @Query("""
            SELECT DISTINCT pggct.khachHang.id
            FROM PhieuGiamGiaChiTiet pggct
            JOIN pggct.khachHang kh
            WHERE pggct.phieuGiamGia.id = :id
              AND (:search IS NULL OR kh.ten LIKE %:search% OR kh.sdt LIKE %:search%)
            """)
    Page<String> getDanhSachKhachHang(@Param("id") String id, @Param("search") String search, Pageable pageable);

    @Query("select distinct p.id from PhieuGiamGia p where p.ten = :ten")
    String checkThemPhieu(@Param("ten") String ten);
}
