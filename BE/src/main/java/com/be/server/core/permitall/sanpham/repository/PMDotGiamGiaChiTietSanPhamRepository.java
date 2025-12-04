package com.be.server.core.permitall.sanpham.repository;

import com.be.server.entity.DotGiamGiaChiTietSanPham;
import com.be.server.repository.DotGiamGiaChiTietSanPhamRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PMDotGiamGiaChiTietSanPhamRepository extends DotGiamGiaChiTietSanPhamRepository {

    @Query("""
        SELECT dggctsp FROM DotGiamGiaChiTietSanPham dggctsp
        WHERE dggctsp.sanPhamChiTiet.id = :idSanPhamChiTiet
          AND dggctsp.dotGiamGia.ngayBatDau <= :now
          AND dggctsp.dotGiamGia.ngayKetThuc >= :now
          AND dggctsp.trangThai = 'DANG_SU_DUNG'
          AND dggctsp.dotGiamGia.trangThai = 'DANG_KICH_HOAT'
        ORDER BY dggctsp.dotGiamGia.phanTramGiam DESC
    """)
    Optional<DotGiamGiaChiTietSanPham> findHieuLucBySanPhamChiTietId(
            @Param("idSanPhamChiTiet") String idSanPhamChiTiet,
            @Param("now") Long now
    );

}
