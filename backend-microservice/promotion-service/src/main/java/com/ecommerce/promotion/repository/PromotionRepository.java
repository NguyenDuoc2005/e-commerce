package com.ecommerce.promotion.repository;

import com.ecommerce.promotion.entity.PromotionCampaign;
import com.ecommerce.promotion.constant.CampaignType;
import com.ecommerce.promotion.model.request.FindPromotionRequest;
import com.ecommerce.promotion.model.response.PromotionByIdResponse;
import com.ecommerce.promotion.model.response.PromotionByProductDetail;
import com.ecommerce.promotion.model.response.PromotionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PromotionRepository extends JpaRepository<PromotionCampaign, String> {

    @Query(value = """
            SELECT
                dgg.id AS id,
                dgg.code AS code,
                dgg.name AS name,
                dgg.discount_value AS discountValue,
                dgg.description AS description,
                dgg.start_date AS startDate,
                dgg.end_date AS endDate,
                dgg.campaign_status AS trangThai,
                dgg.seller_id AS sellerId
            FROM promotion_campaign dgg
            WHERE (:#{#req.code} IS NULL OR :#{#req.code} = '' OR dgg.code LIKE %:#{#req.code}% OR dgg.name LIKE %:#{#req.code}%)
              AND (dgg.campaign_type IS NULL OR dgg.campaign_type = 'STANDARD')
              AND (:#{#req.discountValue} IS NULL OR dgg.discount_value = :#{#req.discountValue})
              AND (:#{#req.trangThai} IS NULL OR dgg.campaign_status = :#{#req.trangThai})
              AND ((:#{#req.startDate} IS NULL OR :#{#req.endDate} IS NULL) OR (dgg.start_date >= :#{#req.startDate} AND dgg.end_date <= :#{#req.endDate}))
              AND (:#{#req.platformOnly != true} = TRUE OR dgg.seller_id IS NULL)
              AND (:#{#req.sellerId == null || #req.sellerId.isEmpty()} = TRUE OR dgg.seller_id = :#{#req.sellerId})
            GROUP BY dgg.id
            ORDER BY dgg.last_modified_date DESC
            """, nativeQuery = true)
    Page<PromotionResponse> getAllPromotionCampaign(@Param("req") FindPromotionRequest req, Pageable pageable);

    @Query(value = """
            SELECT
                po.id AS id,
                po.code AS code,
                po.name AS name,
                po.discount_value AS value,
                po.start_date AS startDate,
                po.end_date AS endDate,
                po.campaign_status AS status,
                (SELECT GROUP_CONCAT(DISTINCT ppd2.product_variant_id) FROM promotion_campaign_product ppd2 WHERE ppd2.detail_status = 'DANG_SU_DUNG' AND ppd2.promotion_campaign_id = po.id) AS productDetail,
                GROUP_CONCAT(DISTINCT ppd.product_variant_id) AS productDetailUpdate,
                NULL AS product,
                GROUP_CONCAT(DISTINCT ppd.id) AS promotionProductDetail
            FROM promotion_campaign po
                LEFT JOIN promotion_campaign_product ppd ON po.id = ppd.promotion_campaign_id
            WHERE po.id = :id
            GROUP BY po.id
            """, nativeQuery = true)
    PromotionByIdResponse getByIdPromotion(@Param("id") String id);

    Optional<PromotionCampaign> findByName(String name);

    @Query("SELECT d FROM PromotionCampaign d JOIN PromotionCampaignProduct dc ON d.id = dc.promotionCampaign.id WHERE dc.productVariantId IN :productDetailIds")
    List<PromotionCampaign> findAllByProductDetails(List<String> productDetailIds);

    List<PromotionCampaign> findByCampaignTypeOrderByStartDateDesc(CampaignType campaignType);
}
