package com.dw.backend.doablewellbeingbackend.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_checkins")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class CheckinEntity {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String level; // comfort / stretch / burnout

    @Column(nullable = false)
    private int intensity; // 0–100

    private String note;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;
}