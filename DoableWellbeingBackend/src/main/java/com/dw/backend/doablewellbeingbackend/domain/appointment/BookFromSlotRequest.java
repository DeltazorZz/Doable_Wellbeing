package com.dw.backend.doablewellbeingbackend.domain.appointment;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;
import java.util.UUID;

public record BookFromSlotRequest(
        @NotNull UUID coachId,
        @NotNull OffsetDateTime slotStart,
        @Min(15) @Max(120) int durationMinutes,
        String notes
) {}