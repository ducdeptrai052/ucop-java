package com.ucop.entity;

import com.ucop.security.SecurityContext;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

import java.time.LocalDateTime;

public class AuditListener {

    @PrePersist
    public void prePersist(AuditableEntity entity) {
        LocalDateTime now = LocalDateTime.now();
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        String user = SecurityContext.getCurrentUsername();
        entity.setCreatedBy(user);
        entity.setUpdatedBy(user);
    }

    @PreUpdate
    public void preUpdate(AuditableEntity entity) {
        entity.setUpdatedAt(LocalDateTime.now());
        entity.setUpdatedBy(SecurityContext.getCurrentUsername());
    }
}
