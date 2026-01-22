package com.dw.backend.doablewellbeingbackend.domain.appointment;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;

@Value
@Builder
public class SlotView {
    LocalDate date;
    LocalTime startTime;
    LocalTime endTime;
    OffsetDateTime startsAt;
    OffsetDateTime endsAt;
}
