package com.dw.backend.doablewellbeingbackend.presistence.entity;


import com.dw.backend.doablewellbeingbackend.domain.enums.AppointmentStatus;
import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.type.SqlTypes;

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
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private AppointmentStatus status;

    private String notes;
}
