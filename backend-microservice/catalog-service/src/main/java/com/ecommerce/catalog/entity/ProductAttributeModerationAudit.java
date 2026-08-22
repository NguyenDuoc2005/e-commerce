package com.ecommerce.catalog.entity;

import com.ecommerce.catalog.entity.base.PrimaryEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = "product_attribute_moderation_audit", indexes = {
        @Index(name = "idx_attribute_audit_source", columnList = "source_attribute_id, created_date")
})
public class ProductAttributeModerationAudit extends PrimaryEntity {

    @Column(nullable = false, length = 30)
    private String action;

    @Column(name = "actor_user_id", length = 36)
    private String actorUserId;

    @Column(name = "source_attribute_id", nullable = false, length = 36)
    private String sourceAttributeId;

    @Column(name = "target_attribute_id", length = 36)
    private String targetAttributeId;

    @Column(length = 1000)
    private String reason;

    @Column(name = "affected_product_count", nullable = false)
    private Integer affectedProductCount = 0;

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getActorUserId() { return actorUserId; }
    public void setActorUserId(String actorUserId) { this.actorUserId = actorUserId; }
    public String getSourceAttributeId() { return sourceAttributeId; }
    public void setSourceAttributeId(String sourceAttributeId) { this.sourceAttributeId = sourceAttributeId; }
    public String getTargetAttributeId() { return targetAttributeId; }
    public void setTargetAttributeId(String targetAttributeId) { this.targetAttributeId = targetAttributeId; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public Integer getAffectedProductCount() { return affectedProductCount; }
    public void setAffectedProductCount(Integer affectedProductCount) { this.affectedProductCount = affectedProductCount; }
}
