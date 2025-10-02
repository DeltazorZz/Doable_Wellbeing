package com.dw.doablewellbeingbackend.presistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "session_notes")
@NoArgsConstructor @AllArgsConstructor @Builder
public class SessionNoteEntity {

    @Id
    private UUID id;

    @JoinColumn(name = "appointment_id", nullable = false)
    private UUID appointmentId;

    @JoinColumn(name = "coach_id", nullable = false)
    private UUID coachId;

    private String note;
    private OffsetDateTime createdAt;
}
