package com.ecommerce.payout.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "seller_wallet")
public class SellerWallet {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "seller_id", nullable = false, unique = true, length = 36)
    private String sellerId;

    @Column(name = "pending_amount", nullable = false)
    private Double pendingAmount = 0D;

    @Column(name = "available_amount", nullable = false)
    private Double availableAmount = 0D;

    @Column(name = "paid_amount", nullable = false)
    private Double paidAmount = 0D;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        if (id == null || id.isBlank()) {
            id = UUID.randomUUID().toString();
        }
        if (updatedAt == null) {
            updatedAt = Instant.now();
        }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getSellerId() { return sellerId; }
    public void setSellerId(String sellerId) { this.sellerId = sellerId; }
    public Double getPendingAmount() { return pendingAmount; }
    public void setPendingAmount(Double pendingAmount) { this.pendingAmount = pendingAmount; }
    public Double getAvailableAmount() { return availableAmount; }
    public void setAvailableAmount(Double availableAmount) { this.availableAmount = availableAmount; }
    public Double getPaidAmount() { return paidAmount; }
    public void setPaidAmount(Double paidAmount) { this.paidAmount = paidAmount; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
