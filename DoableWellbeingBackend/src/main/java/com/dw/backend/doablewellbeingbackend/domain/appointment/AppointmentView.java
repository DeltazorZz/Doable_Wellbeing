package com.dw.backend.doablewellbeingbackend.domain.appointment;


import com.dw.backend.doablewellbeingbackend.domain.enums.AppointmentStatus;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data @Builder  @AllArgsConstructor
@NoArgsConstructor
public class AppointmentView {
    private UUID id;
    private UUID coachId;
    private UUID clientId;
    private OffsetDateTime startsAt;
    private OffsetDateTime endsAt;
    private AppointmentStatus status;
    private String notes;
}
