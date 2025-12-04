package com.be.server.entity.base;

import com.be.server.infrastructure.listener.AuditEntityListener;
import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditEntityListener.class)
public abstract class AuditEntity {

    @Column(name = "created_date",updatable = false)
    private Long createdDate;

    @Column
    private Long lastModifiedDate;

}
