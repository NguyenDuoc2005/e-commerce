package com.ecommerce.payout.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payout_batch")
public class PayoutBatch {
    @Id @Column(length = 36)
    private String id;
    @Column(name = "reference_code", nullable = false, unique = true, length = 40)
    private String referenceCode;
    @Column(name = "status", nullable = false, length = 20)
    private String status = "PAID";
    @Column(name = "item_count", nullable = false)
    private Integer itemCount;
    @Column(name = "total_amount", nullable = false)
    private Double totalAmount;
    @Column(name = "created_by_staff_id", length = 36)
    private String createdByStaffId;
    @Column(name = "note", length = 500)
    private String note;
    @Column(name = "created_at")
    private Instant createdAt;
    @Column(name = "paid_at")
    private Instant paidAt;

    @PrePersist void onCreate() {
        if (id == null || id.isBlank()) id = UUID.randomUUID().toString();
        if (createdAt == null) createdAt = Instant.now();
        if (paidAt == null) paidAt = createdAt;
        if (referenceCode == null || referenceCode.isBlank()) referenceCode = "PAY-" + id.substring(0, 8).toUpperCase();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getReferenceCode() { return referenceCode; }
    public void setReferenceCode(String referenceCode) { this.referenceCode = referenceCode; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getItemCount() { return itemCount; }
    public void setItemCount(Integer itemCount) { this.itemCount = itemCount; }
    public Double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }
    public String getCreatedByStaffId() { return createdByStaffId; }
    public void setCreatedByStaffId(String createdByStaffId) { this.createdByStaffId = createdByStaffId; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getPaidAt() { return paidAt; }
    public void setPaidAt(Instant paidAt) { this.paidAt = paidAt; }
}
