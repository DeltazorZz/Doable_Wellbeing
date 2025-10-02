package com.dw.doablewellbeingbackend.domain.appointment;


import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data @Builder
public class AppointmentView {
    private UUID id;
    private UUID coachId;
    private UUID clientId;
    private OffsetDateTime startsAt;
    private OffsetDateTime endsAt;
    private String status;
    private String notes;
}
