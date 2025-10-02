package com.dw.doablewellbeingbackend.presistence.entity;

import com.dw.doablewellbeingbackend.domain.enums.AppointmentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "appointments")
public class AppointmentEntity {
    @Id
    private UUID id;


    @JoinColumn(name = "coach_id")
    private UUID coachId;


    @JoinColumn(name = "client_id")
    private UUID clientId;

    private OffsetDateTime startsAt;
    private OffsetDateTime endsAt;

    @Enumerated(EnumType.STRING)
    private AppointmentStatus status;

    private String notes;
}
