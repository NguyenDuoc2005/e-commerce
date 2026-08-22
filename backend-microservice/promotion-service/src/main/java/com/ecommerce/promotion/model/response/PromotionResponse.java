package com.ecommerce.promotion.model.response;

public interface PromotionResponse {
    String getId();
    String getCode();
    String getName();
    Double getDiscountValue();
    String getDescription();
    String getTrangThai();
    Long getStartDate();
    Long getEndDate();
    String getSellerId();
}
