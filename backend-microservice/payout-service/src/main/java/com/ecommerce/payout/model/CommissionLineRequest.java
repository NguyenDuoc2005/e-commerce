package com.ecommerce.payout.model;

public class CommissionLineRequest {
    private String categoryId;
    private Double grossAmount;

    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }
    public Double getGrossAmount() { return grossAmount; }
    public void setGrossAmount(Double grossAmount) { this.grossAmount = grossAmount; }
}
