package com.ecommerce.promotion.model.response;

public interface VoucherResponse {
    String getId();
    String getCode();
    String getName();
    Double getDiscountValue();
    Integer getQuantity();
    String getStartDate();
    String getEndDate();
    Double getConditionAmount();
    Double getMaxDiscountAmount();
    Boolean getDiscountType();
    Boolean getDiscountMethod();
    String getIdKH();
    String getStatus();
    String getSellerId();
}
