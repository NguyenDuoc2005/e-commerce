package com.ecommerce.catalog.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "product_attribute_moderation_audit", indexes = {
        @Index(name = "idx_attribute_audit_source_created", columnList = "source_definition_id, created_date")
})
public class ProductAttributeModerationAudit {

    @Id
    @Column(length = 36, nullable = false, updatable = false)
    private String id;

    @Column(nullable = false, length = 30)
    private String action;

    @Column(name = "actor_user_id", length = 36)
    private String actorUserId;

    @Column(name = "source_definition_id", nullable = false, length = 36)
    private String sourceDefinitionId;

    @Column(name = "target_definition_id", length = 36)
    private String targetDefinitionId;

    @Column(length = 1000)
    private String reason;

    @Column(name = "affected_product_count", nullable = false)
    private Integer affectedProductCount = 0;

    @Column(name = "created_date", nullable = false)
    private Long createdDate;

    @PrePersist
    void onCreate() {
        if (id == null) id = UUID.randomUUID().toString();
        if (createdDate == null) createdDate = System.currentTimeMillis();
    }

    public String getId() { return id; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getActorUserId() { return actorUserId; }
    public void setActorUserId(String actorUserId) { this.actorUserId = actorUserId; }
    public String getSourceDefinitionId() { return sourceDefinitionId; }
    public void setSourceDefinitionId(String sourceDefinitionId) { this.sourceDefinitionId = sourceDefinitionId; }
    public String getTargetDefinitionId() { return targetDefinitionId; }
    public void setTargetDefinitionId(String targetDefinitionId) { this.targetDefinitionId = targetDefinitionId; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public Integer getAffectedProductCount() { return affectedProductCount; }
    public void setAffectedProductCount(Integer affectedProductCount) { this.affectedProductCount = affectedProductCount; }
    public Long getCreatedDate() { return createdDate; }
}
