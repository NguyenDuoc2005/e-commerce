package com.be.server.infrastructure.listener;

import com.be.server.entity.base.PrimaryEntity;
import com.be.server.infrastructure.constant.EntityStatus;
import jakarta.persistence.PrePersist;

import java.util.UUID;

public class CreatePrimaryEntityListener {

    @PrePersist
    private void onCreate(PrimaryEntity entity) {
        entity.setId(UUID.randomUUID().toString());
    }

}
