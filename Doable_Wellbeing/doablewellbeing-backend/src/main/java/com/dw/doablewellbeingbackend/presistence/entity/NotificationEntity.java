package com.dw.doablewellbeingbackend.presistence.entity;

import jakarta.persistence.*;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;


@Entity
@Table(name = "notifications")
@Data @NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationEntity {
    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @PrePersist void pre() { if (id == null) id = UUID.randomUUID(); }

    @Column(name = "user_id", nullable = false, columnDefinition = "uuid")
    private UUID userId;

    @Column(nullable = false)
    private String type; // pl. appointment_scheduled, appointment_cancelled, new_self_referral

    @Column(columnDefinition = "jsonb", nullable = false)
    private String payload; // JSON string (ld. service)

    @Column(nullable = false)
    private String status = "queued"; // queued/sent/read/failed

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "read_at")
    private OffsetDateTime readAt;
}
