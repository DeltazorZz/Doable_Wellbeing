package com.dw.backend.doablewellbeingbackend.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "mood_logs")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class MoodLogEntity {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "mood_score", nullable = false)
    private int moodScore;

    private String note;

    @Column(name = "logged_at", nullable = false)
    private OffsetDateTime loggedAt;
}