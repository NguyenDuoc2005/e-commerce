package com.ecommerce.order.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "dispute", indexes = {
        @Index(name = "idx_dispute_customer_status", columnList = "customer_id,status"),
        @Index(name = "idx_dispute_seller_status", columnList = "seller_id,status"),
        @Index(name = "idx_dispute_order_seller", columnList = "order_seller_id"),
        @Index(name = "idx_dispute_created_at", columnList = "created_at")
})
public class Dispute {
    @Id @Column(length = 36) private String id;
    @Column(name = "order_seller_id", nullable = false, length = 36) private String orderSellerId;
    @Column(name = "order_id", nullable = false, length = 36) private String orderId;
    @Column(name = "seller_id", nullable = false, length = 36) private String sellerId;
    @Column(name = "customer_id", nullable = false, length = 36) private String customerId;
    @Column(name = "raised_by", nullable = false, length = 15) private String raisedBy;
    @Column(name = "dispute_type", nullable = false, length = 40) private String disputeType;
    @Column(nullable = false, length = 255) private String reason;
    @Column(columnDefinition = "TEXT") private String description;
    @Column(name = "evidence_urls", columnDefinition = "JSON") private String evidenceUrls = "[]";
    @Column(nullable = false, length = 40) private String status = "OPEN";
    @Column(name = "requested_amount") private Double requestedAmount;
    @Column(name = "resolved_amount") private Double resolvedAmount;
    @Column(name = "resolution_note", columnDefinition = "TEXT") private String resolutionNote;
    @Column(name = "resolved_by_staff_id", length = 36) private String resolvedByStaffId;
    @Column(name = "resolved_at") private Instant resolvedAt;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    @PrePersist void onCreate() { if (id == null || id.isBlank()) id = UUID.randomUUID().toString(); if (createdAt == null) createdAt = Instant.now(); updatedAt = createdAt; }
    @PreUpdate void onUpdate() { updatedAt = Instant.now(); }

    public String getId() { return id; } public void setId(String v) { id = v; }
    public String getOrderSellerId() { return orderSellerId; } public void setOrderSellerId(String v) { orderSellerId = v; }
    public String getOrderId() { return orderId; } public void setOrderId(String v) { orderId = v; }
    public String getSellerId() { return sellerId; } public void setSellerId(String v) { sellerId = v; }
    public String getCustomerId() { return customerId; } public void setCustomerId(String v) { customerId = v; }
    public String getRaisedBy() { return raisedBy; } public void setRaisedBy(String v) { raisedBy = v; }
    public String getDisputeType() { return disputeType; } public void setDisputeType(String v) { disputeType = v; }
    public String getReason() { return reason; } public void setReason(String v) { reason = v; }
    public String getDescription() { return description; } public void setDescription(String v) { description = v; }
    public String getEvidenceUrls() { return evidenceUrls; } public void setEvidenceUrls(String v) { evidenceUrls = v; }
    public String getStatus() { return status; } public void setStatus(String v) { status = v; }
    public Double getRequestedAmount() { return requestedAmount; } public void setRequestedAmount(Double v) { requestedAmount = v; }
    public Double getResolvedAmount() { return resolvedAmount; } public void setResolvedAmount(Double v) { resolvedAmount = v; }
    public String getResolutionNote() { return resolutionNote; } public void setResolutionNote(String v) { resolutionNote = v; }
    public String getResolvedByStaffId() { return resolvedByStaffId; } public void setResolvedByStaffId(String v) { resolvedByStaffId = v; }
    public Instant getResolvedAt() { return resolvedAt; } public void setResolvedAt(Instant v) { resolvedAt = v; }
    public Instant getCreatedAt() { return createdAt; } public void setCreatedAt(Instant v) { createdAt = v; }
    public Instant getUpdatedAt() { return updatedAt; } public void setUpdatedAt(Instant v) { updatedAt = v; }
}
