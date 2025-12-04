package com.dw.backend.doablewellbeingbackend.domain.appointment;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class CoachAvailabilityRequest {

    @NotNull
    private LocalDate date;

    @NotNull
    private LocalTime startTime;

    @NotNull
    private LocalTime endTime;

    private boolean recurring;

    @Min(1)
    private Integer repeatWeeks;
}