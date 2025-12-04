package com.dw.backend.doablewellbeingbackend.business.google;

import com.dw.backend.doablewellbeingbackend.persistence.entity.AppointmentEntity;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GoogleMeetService {
    private final Calendar calendar;

    @Value("${google.calendar.calendar-id}")
    private String calendarId;

    public void attachGoogleMeetToAppointment(AppointmentEntity appointment)throws IOException {
        Event event = new Event()
                .setSummary("Doable Wellbeing coaching session")
                .setDescription("Coaching session with Doable Wellbeing");

        DateTime start = new DateTime(appointment.getStartsAt().toInstant().toString());
        DateTime end = new DateTime(appointment.getEndsAt().toInstant().toString());

        event.setStart(new EventDateTime().setDateTime(start));
        event.setEnd(new EventDateTime().setDateTime(end));

        ConferenceData confData = new ConferenceData()
                .setCreateRequest(
                        new CreateConferenceRequest()
                                .setRequestId(UUID.randomUUID().toString())
                                .setConferenceSolutionKey(
                                        new ConferenceSolutionKey().setType("hangoutsMeet")
                                )
                );
        event.setConferenceData(confData);

        Event created = calendar.events()
                .insert(calendarId, event)
                .setConferenceDataVersion(1)
                .execute();
    }
}
