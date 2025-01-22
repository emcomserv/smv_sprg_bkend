package com.smartvehicle.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditEntityListener.class)
public abstract class BaseEntity {

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
