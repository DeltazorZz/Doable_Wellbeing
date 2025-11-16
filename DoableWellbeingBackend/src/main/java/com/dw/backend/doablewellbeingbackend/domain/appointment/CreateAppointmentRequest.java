package com.dw.backend.doablewellbeingbackend.domain.appointment;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data @Builder @AllArgsConstructor
@NoArgsConstructor
public class CreateAppointmentRequest {
    @NotNull
    private UUID coachId;
    @NotNull
    private UUID clientId;
    @NotNull private OffsetDateTime startsAt;
    @NotNull private OffsetDateTime endsAt;
    private String notes;
}
