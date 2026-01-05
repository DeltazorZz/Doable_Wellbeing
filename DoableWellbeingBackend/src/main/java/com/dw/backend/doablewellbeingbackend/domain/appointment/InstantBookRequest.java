package com.dw.backend.doablewellbeingbackend.domain.appointment;

import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;
import java.util.UUID;

public record InstantBookRequest(
        @NotNull UUID coachId,
        @NotNull OffsetDateTime slotStart,
        @NotNull Integer durationMinutes,
        String notes
) {}