package com.be.server.core.permitall.chitietsanpham.repository;

import com.be.server.entity.SanPhamChiTiet;
import com.be.server.repository.SanPhamChiTietRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PMCTSPSanPhamChiTietRepo extends SanPhamChiTietRepository {

    @Query("""
    SELECT ct FROM SanPhamChiTiet ct
    JOIN  ct.kichCo kc
    JOIN  ct.mauSac ms
    JOIN  ct.sanPham sp
    JOIN  sp.thuongHieu th
    JOIN  sp.xuatSu xx
    JOIN  sp.chatLieu cl
    JOIN  sp.danhMuc dm
    JOIN  sp.loaiDe ld
    LEFT JOIN  DotGiamGiaChiTietSanPham dgct ON dgct.sanPhamChiTiet = ct
    LEFT JOIN  dgct.dotGiamGia dgg
    WHERE sp.id = :idSanPham
""")
    List<SanPhamChiTiet> getChiTietSanPhamFull(@Param("idSanPham") String id);




}
