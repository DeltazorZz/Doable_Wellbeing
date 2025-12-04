package com.dw.backend.doablewellbeingbackend.domain.appointment;


import java.time.OffsetDateTime;

public record TimeSlotResponse(
        OffsetDateTime start,
        OffsetDateTime end
) {}