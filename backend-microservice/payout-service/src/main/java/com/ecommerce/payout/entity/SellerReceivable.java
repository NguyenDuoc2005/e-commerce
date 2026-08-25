package com.ecommerce.payout.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "seller_receivable")
public class SellerReceivable {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "order_seller_id", nullable = false, unique = true, length = 36)
    private String orderSellerId;

    @Column(name = "order_id", length = 36)
    private String orderId;

    @Column(name = "seller_id", nullable = false, length = 36)
    private String sellerId;

    @Column(name = "gross_amount", nullable = false)
    private Double grossAmount;

    @Column(name = "commission_rate", nullable = false)
    private Double commissionRate;

    @Column(name = "commission_amount", nullable = false)
    private Double commissionAmount;

    @Column(name = "net_amount", nullable = false)
    private Double netAmount;

    @Column(name = "status", nullable = false, length = 30)
    private String status = "PENDING";

    @Column(name = "available_at")
    private Instant availableAt;

    @Column(name = "released_amount", nullable = false)
    private Double releasedAmount = 0D;

    @Column(name = "paid_at")
    private Instant paidAt;

    @Column(name = "payout_batch_id", length = 36)
    private String payoutBatchId;

    @Column(name = "created_at")
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (id == null || id.isBlank()) {
            id = UUID.randomUUID().toString();
        }
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getOrderSellerId() { return orderSellerId; }
    public void setOrderSellerId(String orderSellerId) { this.orderSellerId = orderSellerId; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getSellerId() { return sellerId; }
    public void setSellerId(String sellerId) { this.sellerId = sellerId; }
    public Double getGrossAmount() { return grossAmount; }
    public void setGrossAmount(Double grossAmount) { this.grossAmount = grossAmount; }
    public Double getCommissionRate() { return commissionRate; }
    public void setCommissionRate(Double commissionRate) { this.commissionRate = commissionRate; }
    public Double getCommissionAmount() { return commissionAmount; }
    public void setCommissionAmount(Double commissionAmount) { this.commissionAmount = commissionAmount; }
    public Double getNetAmount() { return netAmount; }
    public void setNetAmount(Double netAmount) { this.netAmount = netAmount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getAvailableAt() { return availableAt; }
    public void setAvailableAt(Instant availableAt) { this.availableAt = availableAt; }
    public Double getReleasedAmount() { return releasedAmount; }
    public void setReleasedAmount(Double releasedAmount) { this.releasedAmount = releasedAmount; }
    public Instant getPaidAt() { return paidAt; }
    public void setPaidAt(Instant paidAt) { this.paidAt = paidAt; }
    public String getPayoutBatchId() { return payoutBatchId; }
    public void setPayoutBatchId(String payoutBatchId) { this.payoutBatchId = payoutBatchId; }
}
