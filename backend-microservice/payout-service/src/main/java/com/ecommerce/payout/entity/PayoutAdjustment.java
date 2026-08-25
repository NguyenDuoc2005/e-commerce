package com.ecommerce.payout.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payout_adjustment", indexes = {
        @Index(name = "idx_payout_adjustment_seller_created", columnList = "seller_id,created_at"),
        @Index(name = "idx_payout_adjustment_order_seller", columnList = "order_seller_id")
})
public class PayoutAdjustment {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "dispute_id", nullable = false, unique = true, length = 36)
    private String disputeId;

    @Column(name = "order_seller_id", nullable = false, length = 36)
    private String orderSellerId;

    @Column(name = "seller_id", nullable = false, length = 36)
    private String sellerId;

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false, length = 40)
    private String type = "DISPUTE_ADJUSTMENT";

    @Column(nullable = false, length = 30)
    private String status = "APPLIED";

    @Column(length = 500)
    private String reason;

    @Column(name = "created_at")
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (id == null || id.isBlank()) id = UUID.randomUUID().toString();
        if (createdAt == null) createdAt = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getDisputeId() { return disputeId; }
    public void setDisputeId(String disputeId) { this.disputeId = disputeId; }
    public String getOrderSellerId() { return orderSellerId; }
    public void setOrderSellerId(String orderSellerId) { this.orderSellerId = orderSellerId; }
    public String getSellerId() { return sellerId; }
    public void setSellerId(String sellerId) { this.sellerId = sellerId; }
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
