package com.ecommerce.payout.model;

public class CommissionConfigRequest {
    private String categoryId;
    private Double ratePercent;

    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }
    public Double getRatePercent() { return ratePercent; }
    public void setRatePercent(Double ratePercent) { this.ratePercent = ratePercent; }
}
