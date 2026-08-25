package com.ecommerce.payout.model;

public class DisputeAdjustmentRequest {
    private String disputeId;
    private String orderSellerId;
    private String sellerId;
    private Double refundAmount;
    private String reason;

    public String getDisputeId() { return disputeId; }
    public void setDisputeId(String disputeId) { this.disputeId = disputeId; }
    public String getOrderSellerId() { return orderSellerId; }
    public void setOrderSellerId(String orderSellerId) { this.orderSellerId = orderSellerId; }
    public String getSellerId() { return sellerId; }
    public void setSellerId(String sellerId) { this.sellerId = sellerId; }
    public Double getRefundAmount() { return refundAmount; }
    public void setRefundAmount(Double refundAmount) { this.refundAmount = refundAmount; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
