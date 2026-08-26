package com.ecommerce.promotion.repository;

import com.ecommerce.promotion.entity.PromotionCampaignProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PromotionDetailRepository extends JpaRepository<PromotionCampaignProduct, String> {
    @Query("select ppd from PromotionCampaignProduct ppd where ppd.productVariantId = :idProductDetail and ppd.promotionCampaign.id = :idPromotion")
    PromotionCampaignProduct getByProductDetailAndPromotion(@Param("idProductDetail") String idProductDetail, @Param("idPromotion") String idPromotion);

    @Query("SELECT pdd FROM PromotionCampaignProduct pdd WHERE pdd.promotionCampaign.id = :idPromotion")
    List<PromotionCampaignProduct> findAllByIdPromotion(@Param("idPromotion") String idPromotion);

    @Query("SELECT pdd.productVariantId FROM PromotionCampaignProduct pdd WHERE pdd.promotionCampaign.id = :idPromotion AND pdd.detailStatus = com.ecommerce.promotion.constant.Status.DANG_SU_DUNG")
    List<String> findActiveProductDetailIdsByPromotion(@Param("idPromotion") String idPromotion);

    @Query("SELECT pdd FROM PromotionCampaignProduct pdd WHERE pdd.productVariantId = :productDetailId")
    List<PromotionCampaignProduct> findAllByProductDetailId(@Param("productDetailId") String productDetailId);

    List<PromotionCampaignProduct> findByPromotionCampaign_IdOrderByCreatedDateDesc(String campaignId);

    List<PromotionCampaignProduct> findBySellerIdOrderByCreatedDateDesc(String sellerId);

    PromotionCampaignProduct findByPromotionCampaign_IdAndProductVariantId(String campaignId, String productVariantId);

    @Query("""
            SELECT pdd
            FROM PromotionCampaignProduct pdd
            WHERE pdd.productVariantId IN :productDetailIds
              AND pdd.detailStatus = com.ecommerce.promotion.constant.Status.DANG_SU_DUNG
              AND pdd.promotionCampaign.trangThai = com.ecommerce.promotion.constant.StatusPromotion.DANG_KICH_HOAT
              AND pdd.promotionCampaign.startDate <= :now
              AND pdd.promotionCampaign.endDate >= :now
            ORDER BY pdd.promotionCampaign.discountValue DESC
            """)
    List<PromotionCampaignProduct> findActiveDiscounts(@Param("productDetailIds") List<String> productDetailIds, @Param("now") Long now);
}
