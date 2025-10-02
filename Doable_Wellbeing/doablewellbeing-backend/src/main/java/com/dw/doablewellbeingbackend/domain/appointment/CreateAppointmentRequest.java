package com.dw.doablewellbeingbackend.domain.appointment;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data @Builder
public class CreateAppointmentRequest {
    @NotNull
    private UUID coachId;
    @NotNull private OffsetDateTime startsAt;
    @NotNull private OffsetDateTime endsAt;
    private String notes;
}
