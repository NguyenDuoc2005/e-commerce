package com.ecommerce.payout.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payout_batch_item")
public class PayoutBatchItem {
    @Id @Column(length = 36)
    private String id;
    @Column(name = "batch_id", nullable = false, length = 36)
    private String batchId;
    @Column(name = "receivable_id", nullable = false, unique = true, length = 36)
    private String receivableId;
    @Column(name = "seller_id", nullable = false, length = 36)
    private String sellerId;
    @Column(name = "amount", nullable = false)
    private Double amount;
    @Column(name = "created_at")
    private Instant createdAt;

    @PrePersist void onCreate() {
        if (id == null || id.isBlank()) id = UUID.randomUUID().toString();
        if (createdAt == null) createdAt = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getBatchId() { return batchId; }
    public void setBatchId(String batchId) { this.batchId = batchId; }
    public String getReceivableId() { return receivableId; }
    public void setReceivableId(String receivableId) { this.receivableId = receivableId; }
    public String getSellerId() { return sellerId; }
    public void setSellerId(String sellerId) { this.sellerId = sellerId; }
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
