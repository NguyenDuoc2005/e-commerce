package com.ecommerce.promotion.service;

import com.ecommerce.common.base.ResponseObject;
import com.ecommerce.promotion.entity.DotGiamGia;
import com.ecommerce.promotion.model.request.CreatePromotionRequest;
import com.ecommerce.promotion.model.request.FindPromotionRequest;
import com.ecommerce.promotion.model.request.UpdatePromotionRequest;
import com.ecommerce.promotion.model.response.PromotionByIdResponse;

import java.util.List;
import java.util.Map;

public interface PromotionService {
    ResponseObject<?> getAll(FindPromotionRequest request);
    List<Map<String, Object>> getSanPham();
    List<Map<String, Object>> getSanPhamCT(String id);
    List<Map<String, Object>> getSanPhamByDot(String id);
    List<Map<String, Object>> getMauSac();
    List<Map<String, Object>> getKichCo();
    DotGiamGia add(CreatePromotionRequest request);
    DotGiamGia update(UpdatePromotionRequest request);
    DotGiamGia updateStatus(String id);
    PromotionByIdResponse getByIdPromotion(String id);
    List<Map<String, Object>> getByIdProductDetail(String id);
}
