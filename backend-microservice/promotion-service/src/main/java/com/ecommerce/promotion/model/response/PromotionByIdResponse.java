package com.ecommerce.promotion.model.response;

public interface PromotionByIdResponse {
    String getId();
    String getCode();
    String getName();
    Double getValue();
    Long getStartDate();
    Long getEndDate();
    String getStatus();
    String getProductDetail();
    String getProductDetailUpdate();
    String getProduct();
    String getPromotionProductDetail();
}
