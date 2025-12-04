package com.dw.backend.doablewellbeingbackend.domain.appointment;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Value
@Builder
public class CoachAvailabilityView {
    UUID id;
    LocalDate date;
    LocalTime startTime;
    LocalTime endTime;
    boolean recurring;
    UUID seriesId;
    boolean active;
}
