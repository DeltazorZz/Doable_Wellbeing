package com.dw.backend.doablewellbeingbackend.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "appointment_notes",
        indexes = {
                @Index(name = "ix_appt_notes_appt_time", columnList = "appointment_id, created_at")
        })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentNoteEntity {

    @Id
    @UuidGenerator
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "appointment_id", nullable = false, columnDefinition = "uuid")
    private UUID appointmentId;

    @Column(name = "created_by", nullable = false, columnDefinition = "uuid")
    private UUID createdBy;

    @Column(name = "note", nullable = false, columnDefinition = "text")
    private String note;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;
}
