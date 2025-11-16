package com.dw.backend.doablewellbeingbackend.domain.appointment;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data @Builder  @AllArgsConstructor
@NoArgsConstructor
public class AppointmentView {
    private UUID id;
    private UUID coachId;
    private UUID clientId;
    private OffsetDateTime startsAt;
    private OffsetDateTime endsAt;
    private String status;
    private String notes;
}
