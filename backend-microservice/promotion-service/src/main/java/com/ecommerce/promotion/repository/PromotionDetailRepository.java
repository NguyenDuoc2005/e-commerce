package com.ecommerce.promotion.repository;

import com.ecommerce.promotion.entity.DotGiamGiaChiTietSanPham;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PromotionDetailRepository extends JpaRepository<DotGiamGiaChiTietSanPham, String> {
    @Query("select ppd from DotGiamGiaChiTietSanPham ppd where ppd.sanPhamChiTiet.id = :idProductDetail and ppd.dotGiamGia.id = :idPromotion")
    DotGiamGiaChiTietSanPham getByProductDetailAndPromotion(@Param("idProductDetail") String idProductDetail, @Param("idPromotion") String idPromotion);

    @Query("SELECT pdd FROM DotGiamGiaChiTietSanPham pdd WHERE pdd.dotGiamGia.id = :idPromotion")
    List<DotGiamGiaChiTietSanPham> findAllByIdPromotion(@Param("idPromotion") String idPromotion);
}
