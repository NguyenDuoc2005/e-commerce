package com.ecommerce.promotion.repository;

import com.ecommerce.promotion.entity.DotGiamGiaChiTietSanPham;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PromotionDetailRepository extends JpaRepository<DotGiamGiaChiTietSanPham, String> {
    @Query("select ppd from DotGiamGiaChiTietSanPham ppd where ppd.sanPhamChiTietId = :idProductDetail and ppd.dotGiamGia.id = :idPromotion")
    DotGiamGiaChiTietSanPham getByProductDetailAndPromotion(@Param("idProductDetail") String idProductDetail, @Param("idPromotion") String idPromotion);

    @Query("SELECT pdd FROM DotGiamGiaChiTietSanPham pdd WHERE pdd.dotGiamGia.id = :idPromotion")
    List<DotGiamGiaChiTietSanPham> findAllByIdPromotion(@Param("idPromotion") String idPromotion);

    @Query("SELECT pdd.sanPhamChiTietId FROM DotGiamGiaChiTietSanPham pdd WHERE pdd.dotGiamGia.id = :idPromotion AND pdd.trangThai = com.ecommerce.promotion.constant.Status.DANG_SU_DUNG")
    List<String> findActiveProductDetailIdsByPromotion(@Param("idPromotion") String idPromotion);

    @Query("SELECT pdd FROM DotGiamGiaChiTietSanPham pdd WHERE pdd.sanPhamChiTietId = :productDetailId")
    List<DotGiamGiaChiTietSanPham> findAllByProductDetailId(@Param("productDetailId") String productDetailId);

    @Query("""
            SELECT pdd
            FROM DotGiamGiaChiTietSanPham pdd
            WHERE pdd.sanPhamChiTietId IN :productDetailIds
              AND pdd.trangThai = com.ecommerce.promotion.constant.Status.DANG_SU_DUNG
              AND pdd.dotGiamGia.trangThai = com.ecommerce.promotion.constant.StatusPromotion.DANG_KICH_HOAT
              AND pdd.dotGiamGia.ngayBatDau <= :now
              AND pdd.dotGiamGia.ngayKetThuc >= :now
            ORDER BY pdd.dotGiamGia.phanTramGiam DESC
            """)
    List<DotGiamGiaChiTietSanPham> findActiveDiscounts(@Param("productDetailIds") List<String> productDetailIds, @Param("now") Long now);
}
