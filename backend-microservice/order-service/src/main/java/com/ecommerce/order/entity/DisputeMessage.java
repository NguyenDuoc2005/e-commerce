package com.ecommerce.order.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "dispute_message", indexes = @Index(name = "idx_dispute_message_created", columnList = "dispute_id,created_at"))
public class DisputeMessage {
    @Id @Column(length = 36) private String id;
    @Column(name = "dispute_id", nullable = false, length = 36) private String disputeId;
    @Column(name = "sender_type", nullable = false, length = 15) private String senderType;
    @Column(name = "sender_id", nullable = false, length = 36) private String senderId;
    @Column(nullable = false, columnDefinition = "TEXT") private String message;
    @Column(name = "attachment_urls", columnDefinition = "JSON") private String attachmentUrls = "[]";
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @PrePersist void onCreate() { if (id == null || id.isBlank()) id = UUID.randomUUID().toString(); if (createdAt == null) createdAt = Instant.now(); }
    public String getId() { return id; } public void setId(String v) { id = v; }
    public String getDisputeId() { return disputeId; } public void setDisputeId(String v) { disputeId = v; }
    public String getSenderType() { return senderType; } public void setSenderType(String v) { senderType = v; }
    public String getSenderId() { return senderId; } public void setSenderId(String v) { senderId = v; }
    public String getMessage() { return message; } public void setMessage(String v) { message = v; }
    public String getAttachmentUrls() { return attachmentUrls; } public void setAttachmentUrls(String v) { attachmentUrls = v; }
    public Instant getCreatedAt() { return createdAt; } public void setCreatedAt(Instant v) { createdAt = v; }
}
