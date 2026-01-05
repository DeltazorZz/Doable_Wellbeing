package com.dw.backend.doablewellbeingbackend.persistence.entity;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_wheel_scores")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class WheelScoreEntity {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String area; // health, work, relationships...

    @Column(nullable = false)
    private int score; // 1–10

    @Column(name = "scored_at", nullable = false)
    private OffsetDateTime scoredAt;
}