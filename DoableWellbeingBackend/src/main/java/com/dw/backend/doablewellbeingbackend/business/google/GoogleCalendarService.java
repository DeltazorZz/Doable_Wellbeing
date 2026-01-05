package com.dw.backend.doablewellbeingbackend.business.google;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.ConferenceData;
import com.google.api.services.calendar.model.ConferenceSolutionKey;
import com.google.api.services.calendar.model.CreateConferenceRequest;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;


@Slf4j
@Service
public class GoogleCalendarService {

    private final HttpTransport httpTransport = new NetHttpTransport();
    private final JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

    @Value("${google.calendar.application-name}")
    private String applicationName;

    @Value("${google.calendar.client-id}")
    private String clientId;

    @Value("${google.calendar.client-secret}")
    private String clientSecret;

    @Value("${google.calendar.refresh-token}")
    private String refreshToken;

    @Value("${google.calendar.calendar-id}")
    private String calendarId;


    private Credential buildCredential() {
        GoogleCredential credential = new GoogleCredential.Builder()
                .setClientSecrets(clientId, clientSecret)
                .setTransport(httpTransport)
                .setJsonFactory(jsonFactory)
                .build();

        credential.setRefreshToken(refreshToken);

        try {
            boolean refreshed = credential.refreshToken();
            if (!refreshed) {
                throw new IllegalStateException("Google token refresh returned false (check refresh_token / client secrets)");
            }
        } catch (IOException e) {
            throw new IllegalStateException("Google token refresh failed: " + e.getMessage(), e);
        }

        return credential;
    }


    protected Calendar buildCalendarClient() {
        Credential credential = buildCredential();
        return new Calendar.Builder(httpTransport, jsonFactory, credential)
                .setApplicationName(applicationName)
                .build();
    }


    public Event createEventWithMeet(
            String summary,
            String description,
            String attendeeEmail,
            ZonedDateTime start,
            ZonedDateTime end,
            String timeZone
    ) throws IOException {

        Calendar calendar = buildCalendarClient();

        Event event = new Event()
                .setSummary(summary)
                .setDescription(description);

        String tz = (timeZone != null && !timeZone.isBlank())
                ? timeZone
                : "Europe/London";
        DateTime startDateTime = new DateTime(start.toInstant().toEpochMilli());
        DateTime endDateTime = new DateTime(end.toInstant().toEpochMilli());

        EventDateTime eventStart = new EventDateTime()
                .setDateTime(startDateTime)
                .setTimeZone(tz);
        EventDateTime eventEnd = new EventDateTime()
                .setDateTime(endDateTime)
                .setTimeZone(tz);

        event.setStart(eventStart);
        event.setEnd(eventEnd);


        if (attendeeEmail != null && !attendeeEmail.isBlank()) {
            EventAttendee attendee = new EventAttendee().setEmail(attendeeEmail);
            event.setAttendees(List.of(attendee));
        }


        ConferenceSolutionKey conferenceSolutionKey = new ConferenceSolutionKey()
                .setType("hangoutsMeet");

        CreateConferenceRequest createConferenceRequest = new CreateConferenceRequest()
                .setRequestId("dw-" + UUID.randomUUID())
                .setConferenceSolutionKey(conferenceSolutionKey);

        ConferenceData conferenceData = new ConferenceData()
                .setCreateRequest(createConferenceRequest);

        event.setConferenceData(conferenceData);


        Event created = calendar.events()
                .insert(calendarId, event)
                .setConferenceDataVersion(1)
                .execute();

        log.info("Created Google Calendar event. id={}, hangoutLink={}",
                created.getId(), created.getHangoutLink());

        return created;
    }

    public Event createEventWithMeet(
            String summary,
            String description,
            String attendeeEmail,
            ZonedDateTime start,
            ZonedDateTime end
    ) throws IOException {
        ZoneId zoneId = start.getZone();
        String tz = (zoneId != null) ? zoneId.getId() : ZoneId.systemDefault().getId();
        return createEventWithMeet(summary, description, attendeeEmail, start, end, tz);
    }

    public void deleteEvent(String eventId) throws IOException {
        Calendar calendar = buildCalendarClient();
        calendar.events()
                .delete(calendarId, eventId)
                .execute();

        log.info("Deleted Google Calendar event. id={}", eventId);
    }


    public Event updateEventTime(
            String eventId,
            ZonedDateTime newStart,
            ZonedDateTime newEnd,
            String timeZone
    ) throws IOException {

        Calendar calendar = buildCalendarClient();

        Event event = calendar.events()
                .get(calendarId, eventId)
                .execute();

        String tz = (timeZone != null && !timeZone.isBlank())
                ? timeZone
                : "Europe/London";

        DateTime startDateTime = new DateTime(newStart.toInstant().toEpochMilli());
        DateTime endDateTime = new DateTime(newEnd.toInstant().toEpochMilli());

        EventDateTime eventStart = new EventDateTime()
                .setDateTime(startDateTime)
                .setTimeZone(tz);
        EventDateTime eventEnd = new EventDateTime()
                .setDateTime(endDateTime)
                .setTimeZone(tz);

        event.setStart(eventStart);
        event.setEnd(eventEnd);

        Event updated = calendar.events()
                .update(calendarId, eventId, event)
                .setConferenceDataVersion(1)
                .execute();

        log.info("Updated Google Calendar event time. id={}", updated.getId());
        return updated;
    }

    public Event getEvent(String eventId) throws IOException {
        Calendar calendar = buildCalendarClient();
        return calendar.events()
                .get(calendarId, eventId)
                .execute();
    }
}
