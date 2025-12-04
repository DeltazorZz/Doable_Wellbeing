package com.dw.backend.doablewellbeingbackend.domain.appointment;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record CoachAvailabilitySlimResponse(
        UUID id,
        LocalDate date,
        LocalTime startTime,
        LocalTime endTime,
        boolean active
) {}

