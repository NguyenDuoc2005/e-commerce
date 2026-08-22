package com.ecommerce.promotion.model.request;

import com.ecommerce.common.base.PageableRequest;

public class FindPromotionRequest extends PageableRequest {
    private String id;
    private String code;
    private String name;
    private Double discountValue;
    private Long startDate;
    private Long endDate;
    private String trangThai;
    private String sellerId;
    private Boolean platformOnly;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Double getDiscountValue() { return discountValue; }
    public void setDiscountValue(Double discountValue) { this.discountValue = discountValue; }
    public Long getStartDate() { return startDate; }
    public void setStartDate(Long startDate) { this.startDate = startDate; }
    public Long getEndDate() { return endDate; }
    public void setEndDate(Long endDate) { this.endDate = endDate; }
    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
    public String getSellerId() { return sellerId; }
    public void setSellerId(String sellerId) { this.sellerId = sellerId; }
    public Boolean getPlatformOnly() { return platformOnly; }
    public void setPlatformOnly(Boolean platformOnly) { this.platformOnly = platformOnly; }
}
