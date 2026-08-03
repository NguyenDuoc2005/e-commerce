package com.ecommerce.promotion.service;

import com.ecommerce.common.base.ResponseObject;
import com.ecommerce.promotion.entity.DotGiamGia;
import com.ecommerce.promotion.entity.SanPham;
import com.ecommerce.promotion.entity.SanPhamChiTiet;
import com.ecommerce.promotion.model.request.CreatePromotionRequest;
import com.ecommerce.promotion.model.request.FindPromotionRequest;
import com.ecommerce.promotion.model.request.UpdatePromotionRequest;
import com.ecommerce.promotion.model.response.PromotionByIdResponse;
import com.ecommerce.promotion.model.response.PromotionByProductDetail;

import java.util.List;

public interface PromotionService {
    ResponseObject<?> getAll(FindPromotionRequest request);
    List<SanPham> getSanPham();
    List<SanPhamChiTiet> getSanPhamCT(String id);
    List<SanPhamChiTiet> getSanPhamByDot(String id);
    DotGiamGia add(CreatePromotionRequest request);
    DotGiamGia update(UpdatePromotionRequest request);
    DotGiamGia updateStatus(String id);
    PromotionByIdResponse getByIdPromotion(String id);
    List<PromotionByProductDetail> getByIdProductDetail(String id);
}
