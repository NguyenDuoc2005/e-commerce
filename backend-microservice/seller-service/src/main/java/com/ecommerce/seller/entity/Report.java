package com.ecommerce.seller.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "report", indexes = {
        @Index(name = "idx_report_target", columnList = "target_type,target_id"),
        @Index(name = "idx_report_status", columnList = "status"),
        @Index(name = "idx_report_created_at", columnList = "created_at")
})
public class Report {
    @Id @Column(length = 36) private String id;
    @Column(name = "reporter_id", nullable = false, length = 36) private String reporterId;
    @Column(name = "reporter_type", nullable = false, length = 16) private String reporterType;
    @Column(name = "target_type", nullable = false, length = 16) private String targetType;
    @Column(name = "target_id", nullable = false, length = 36) private String targetId;
    @Column(name = "reason_code", nullable = false, length = 48) private String reasonCode;
    @Column(columnDefinition = "TEXT") private String description;
    @Column(name = "evidence_urls", columnDefinition = "JSON") private String evidenceUrls = "[]";
    @Column(nullable = false, length = 24) private String status = "PENDING";
    @Column(name = "action_taken", length = 48) private String actionTaken;
    @Column(name = "resolution_note", columnDefinition = "TEXT") private String resolutionNote;
    @Column(name = "reviewed_by_staff_id", length = 36) private String reviewedByStaffId;
    @Column(name = "reviewed_at") private Instant reviewedAt;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;

    @PrePersist void onCreate() { if (id == null || id.isBlank()) id = UUID.randomUUID().toString(); if (createdAt == null) createdAt = Instant.now(); }
    public String getId() { return id; } public void setId(String v) { id = v; }
    public String getReporterId() { return reporterId; } public void setReporterId(String v) { reporterId = v; }
    public String getReporterType() { return reporterType; } public void setReporterType(String v) { reporterType = v; }
    public String getTargetType() { return targetType; } public void setTargetType(String v) { targetType = v; }
    public String getTargetId() { return targetId; } public void setTargetId(String v) { targetId = v; }
    public String getReasonCode() { return reasonCode; } public void setReasonCode(String v) { reasonCode = v; }
    public String getDescription() { return description; } public void setDescription(String v) { description = v; }
    public String getEvidenceUrls() { return evidenceUrls; } public void setEvidenceUrls(String v) { evidenceUrls = v; }
    public String getStatus() { return status; } public void setStatus(String v) { status = v; }
    public String getActionTaken() { return actionTaken; } public void setActionTaken(String v) { actionTaken = v; }
    public String getResolutionNote() { return resolutionNote; } public void setResolutionNote(String v) { resolutionNote = v; }
    public String getReviewedByStaffId() { return reviewedByStaffId; } public void setReviewedByStaffId(String v) { reviewedByStaffId = v; }
    public Instant getReviewedAt() { return reviewedAt; } public void setReviewedAt(Instant v) { reviewedAt = v; }
    public Instant getCreatedAt() { return createdAt; } public void setCreatedAt(Instant v) { createdAt = v; }
}
