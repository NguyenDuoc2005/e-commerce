package com.ecommerce.auth.entity.base;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

@MappedSuperclass
public abstract class AuditEntity {

    @Column(name = "created_date", updatable = false)
    private Long createdDate;

    @Column
    private Long lastModifiedDate;

    @PrePersist
    void onCreateAudit() {
        long now = System.currentTimeMillis();
        this.createdDate = now;
        this.lastModifiedDate = now;
    }

    @PreUpdate
    void onUpdateAudit() {
        this.lastModifiedDate = System.currentTimeMillis();
    }

    public Long getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Long createdDate) {
        this.createdDate = createdDate;
    }

    public Long getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(Long lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }
}
