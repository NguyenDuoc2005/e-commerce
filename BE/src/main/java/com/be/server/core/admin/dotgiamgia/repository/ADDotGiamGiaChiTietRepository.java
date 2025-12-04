package com.be.server.core.admin.dotgiamgia.repository;

import com.be.server.entity.DotGiamGia;
import com.be.server.entity.DotGiamGiaChiTietSanPham;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ADDotGiamGiaChiTietRepository extends JpaRepository<DotGiamGiaChiTietSanPham, String> {

    @Query(value = "select ppd from DotGiamGiaChiTietSanPham ppd where ppd.sanPhamChiTiet.id =:idProductDetail and ppd.dotGiamGia.id =:idPromotion")
    DotGiamGiaChiTietSanPham getByProductDetailAndPromotion(@Param("idProductDetail")String idProductDetail, @Param("idPromotion") String idPromotion );

    @Query("SELECT pdd FROM DotGiamGiaChiTietSanPham  pdd WHERE pdd.dotGiamGia.id = :idPromotion")
    List<DotGiamGiaChiTietSanPham> findAllByIdPromotion(@Param("idPromotion") String idPromotion);
}