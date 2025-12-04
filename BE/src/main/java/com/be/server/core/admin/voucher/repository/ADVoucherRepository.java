package com.be.server.core.admin.voucher.repository;



import com.be.server.core.admin.SanPhamChiTiet.model.response.ADThemSanPhamChiTietResponse;
import com.be.server.core.admin.sanpham.model.request.ADSanPhamSearchRequest;
import com.be.server.core.admin.sanpham.model.response.ADSanPhamResponse;
import com.be.server.core.admin.voucher.model.request.ADVoucherSearchRequest;
import com.be.server.core.admin.voucher.model.response.ADPhieuGiamGiaResponse;


import com.be.server.entity.PhieuGiamGia;
import com.be.server.entity.ThuongHieu;
import com.be.server.repository.VoucherRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

@Repository
public interface ADVoucherRepository extends VoucherRepository {

    @Query("""
    SELECT DISTINCT p 
    FROM PhieuGiamGia p
        LEFT JOIN PhieuGiamGiaChiTiet pggct ON p.id = pggct.phieuGiamGia.id 
    WHERE p.status = 0
      AND p.soLuongPhieu > 0
      AND (
          p.loaiGiam = FALSE
          OR (p.loaiGiam = TRUE AND pggct.khachHang.id = :id)
      )
      AND NOT EXISTS (
          SELECT 1 FROM HoaDon hd
          WHERE hd.voucher.id = p.id
            AND hd.khachHang.id = :id
            AND hd.trangThaiHoaDon = 4
      )
""")
    List<PhieuGiamGia> findAvailableVouchers(@Param("id") String id);


    @Query(value = """
        SELECT
            pgg.id as id,
            pgg.ma as ma,
            pgg.ten as ten,
            pgg.dieuKien as dieuKien,
            pgg.giaGiam as giaGiam,
            pgg.kieuGiam as kieuGiam,
            pgg.loaiGiam as loaiGiam,
            pgg.phanTramGiam as phanTramGiam,
            pgg.soLuongPhieu as soLuongPhieu,
            pgg.ngayBatDau as ngayBatDau,
            pgg.ngayKetThuc as ngayKetThuc,
            pgg.status as status   
        FROM
            PhieuGiamGia pgg
        WHERE
            (
                :#{#req.q == null || #req.q.isEmpty()} = TRUE OR
                LOWER(pgg.ma) LIKE LOWER(CONCAT('%', :#{#req.q}, '%')) OR
                LOWER(pgg.ten) LIKE LOWER(CONCAT('%', :#{#req.q}, '%'))
            )
            AND (:#{#req.startDate == null} = TRUE OR pgg.ngayBatDau >= :#{#req.startDate})
            AND (:#{#req.endDate == null} = TRUE OR pgg.ngayKetThuc <= :#{#req.endDate})
            AND (:#{#req.kieuGiam == null} = TRUE OR pgg.kieuGiam = :#{#req.kieu})
            AND (:#{#req.status == null} = TRUE OR pgg.status = :#{#req.entityStatus})
            
        ORDER BY pgg.createdDate DESC
    """,
            countQuery = """
        SELECT
            COUNT(pgg.id)
        FROM
            PhieuGiamGia pgg
        WHERE
            (
                :#{#req.q == null || #req.q.isEmpty()} = TRUE OR
                LOWER(pgg.ma) LIKE LOWER(CONCAT('%', :#{#req.q}, '%')) OR
                LOWER(pgg.ten) LIKE LOWER(CONCAT('%', :#{#req.q}, '%'))
            )
            AND (:#{#req.startDate == null} = TRUE OR pgg.ngayBatDau >= :#{#req.startDate})
            AND (:#{#req.endDate == null} = TRUE OR pgg.ngayKetThuc <= :#{#req.endDate})
            AND (:#{#req.kieuGiam == null} = TRUE OR pgg.kieuGiam = :#{#req.kieu})
            AND (:#{#req.status == null} = TRUE OR pgg.status = :#{#req.entityStatus})
"""
    )
    Page<ADPhieuGiamGiaResponse> getAllPhieuGiamGiaFilter(Pageable pageable, @Param("req") ADVoucherSearchRequest req);


    @Query(value = """
        SELECT 
          pgg.id as id,      
          pgg.ma as ma, 
          pgg.ten as ten,
          pgg.dieuKien as dieuKien,
          pgg.giaGiam as giaGiam,
          pgg.kieuGiam as kieuGiam,
          pgg.loaiGiam as loaiGiam,
          pgg.phanTramGiam as phanTramGiam,
          pgg.soLuongPhieu  as soLuongPhieu,
          pgg.ngayBatDau as ngayBatDau,
          pgg.ngayKetThuc as ngayKetThuc                                                                    
        FROM 
            PhieuGiamGia  pgg
        WHERE 
            pgg.id LIKE CONCAT('%', :#{#rep}, '%')           
        """ )
    Optional<ADPhieuGiamGiaResponse> getNhanVienID(@Param("rep") String id);

    @Query("""
    SELECT DISTINCT pggct.khachHang.id 
    FROM PhieuGiamGiaChiTiet pggct
    JOIN pggct.khachHang kh
    WHERE pggct.phieuGiamGia.id = :id
    AND (:search IS NULL OR kh.ten LIKE %:search% OR kh.sdt LIKE %:search%)
""")
    Page<String> getDanhSachKhachHang(@Param("id") String id, @Param("search") String search, Pageable pageable);

    @Query("""
    select distinct spct.id
    from PhieuGiamGia spct
    where spct.ten = :tenSP
""")
    String checkThemPhieu(
            @Param("tenSP") String tenSP
    );



}
