package com.ecommerce.promotion.repository;

import com.ecommerce.promotion.entity.SanPhamChiTiet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SanPhamChiTietRepository extends JpaRepository<SanPhamChiTiet, String> {
    @Query("SELECT spct FROM SanPhamChiTiet spct JOIN SanPham sp ON sp.id = spct.sanPham.id WHERE sp.id = :idSP AND spct.status = 0")
    List<SanPhamChiTiet> findBySanPham(String idSP);

    @Query("""
            SELECT spct
            FROM SanPhamChiTiet spct
            JOIN SanPham sp ON sp.id = spct.sanPham.id
            LEFT JOIN DotGiamGiaChiTietSanPham dggctsp ON dggctsp.sanPhamChiTiet.id = spct.id
            LEFT JOIN DotGiamGia dgg ON dgg.id = dggctsp.dotGiamGia.id
            WHERE sp.id = :idSP
              AND spct.status = 0
              AND (dgg.trangThai IS NULL OR dgg.trangThai != 'DANG_KICH_HOAT')
            """)
    List<SanPhamChiTiet> detailSPCTByDot(String idSP);
}
