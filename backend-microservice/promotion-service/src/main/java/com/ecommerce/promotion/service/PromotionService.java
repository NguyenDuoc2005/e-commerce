package com.ecommerce.promotion.service;

import com.ecommerce.common.base.ResponseObject;
import com.ecommerce.promotion.entity.PromotionCampaign;
import com.ecommerce.promotion.model.request.CreatePromotionRequest;
import com.ecommerce.promotion.model.request.FindPromotionRequest;
import com.ecommerce.promotion.model.request.UpdatePromotionRequest;
import com.ecommerce.promotion.model.response.PromotionByIdResponse;

import java.util.List;
import java.util.Map;

public interface PromotionService {
    ResponseObject<?> getAll(FindPromotionRequest request);
    List<Map<String, Object>> getProduct();
    List<Map<String, Object>> getProductVariants(String productId);
    List<Map<String, Object>> getProductVariantsByCampaign(String campaignId);
    PromotionCampaign add(CreatePromotionRequest request);
    PromotionCampaign update(UpdatePromotionRequest request);
    PromotionCampaign updateStatus(String id);
    ResponseObject<?> getSellerAll(String sellerId, FindPromotionRequest request);
    PromotionCampaign addSeller(String sellerId, CreatePromotionRequest request);
    PromotionCampaign updateSeller(String sellerId, UpdatePromotionRequest request);
    PromotionCampaign updateSellerStatus(String sellerId, String id);
    PromotionByIdResponse getByIdPromotion(String id);
}
