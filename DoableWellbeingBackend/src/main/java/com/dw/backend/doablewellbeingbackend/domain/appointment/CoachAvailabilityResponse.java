package com.dw.backend.doablewellbeingbackend.domain.appointment;

import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Builder
public record CoachAvailabilityResponse(
        UUID id,
        LocalDate date,
        LocalTime startTime,
        LocalTime endTime,
        boolean recurring,
        UUID seriesId,
        boolean active
) {}




