package com.ecommerce.payout.model;

public class ReceivableRequest {
    private String orderSellerId;
    private String orderId;
    private String sellerId;
    private Double grossAmount;
    private String categoryId;
    private java.util.List<CommissionLineRequest> commissionLines;

    public String getOrderSellerId() { return orderSellerId; }
    public void setOrderSellerId(String orderSellerId) { this.orderSellerId = orderSellerId; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getSellerId() { return sellerId; }
    public void setSellerId(String sellerId) { this.sellerId = sellerId; }
    public Double getGrossAmount() { return grossAmount; }
    public void setGrossAmount(Double grossAmount) { this.grossAmount = grossAmount; }
    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }
    public java.util.List<CommissionLineRequest> getCommissionLines() { return commissionLines; }
    public void setCommissionLines(java.util.List<CommissionLineRequest> commissionLines) { this.commissionLines = commissionLines; }
}
