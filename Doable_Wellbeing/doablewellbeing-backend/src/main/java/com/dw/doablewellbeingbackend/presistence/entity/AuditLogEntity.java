package com.dw.doablewellbeingbackend.presistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "audit_logs")
@Data
@Builder
@NoArgsConstructor @AllArgsConstructor
public class AuditLogEntity {
    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @PrePersist void pre() { if (id == null) id = UUID.randomUUID(); }

    @Column(name = "user_id", columnDefinition = "uuid")
    private UUID userId;

    @Column(nullable = false) private String action;
    @Column(nullable = false) private String entity;
    @Column(name = "entity_id", columnDefinition = "uuid")
    private UUID entityId;

    @Column(columnDefinition = "jsonb", nullable = false)
    private String meta = "{}"; // JSON string

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();
}
