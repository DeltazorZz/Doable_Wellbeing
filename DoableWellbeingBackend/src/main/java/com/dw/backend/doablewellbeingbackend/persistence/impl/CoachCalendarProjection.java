package com.dw.backend.doablewellbeingbackend.persistence.impl;


import com.dw.backend.doablewellbeingbackend.domain.enums.AppointmentStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface CoachCalendarProjection {
    UUID getId();
    OffsetDateTime getStartsAt();
    OffsetDateTime getEndsAt();
    AppointmentStatus getStatus();
    String getMeetingUrl();
    String getExternalCalendarId();

    UUID getClientId();
    String getClientName();
    String getClientEmail();
    String getNotes();
}
