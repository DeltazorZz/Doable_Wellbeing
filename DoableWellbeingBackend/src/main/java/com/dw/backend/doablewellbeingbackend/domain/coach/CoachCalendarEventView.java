package com.dw.backend.doablewellbeingbackend.domain.coach;


import com.dw.backend.doablewellbeingbackend.domain.enums.AppointmentStatus;

import java.time.OffsetDateTime;
import java.util.UUID;


public record CoachCalendarEventView(
        UUID id,
        String title,
        OffsetDateTime startsAt,
        OffsetDateTime endsAt,
        AppointmentStatus status,
        String meetingUrl,
        String externalCalendarId,
        ClientSummaryView client,
        String notesPreview
) {}