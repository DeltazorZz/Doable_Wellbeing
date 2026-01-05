package com.dw.backend.doablewellbeingbackend.business.google;

import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GoogleCalendarServiceTest {

    private GoogleCalendarService service;

    @Mock private Calendar calendar;
    @Mock private Calendar.Events events;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        // Spy → hogy a buildCalendarClient()-et felül tudjuk írni (ezért kell protected)
        service = spy(new GoogleCalendarService());

        // Inject calendarId (mert @Value nincs unit tesztben)
        ReflectionTestUtils.setField(service, "calendarId", "primary");

        // buildCalendarClient() -> mock calendar
        doReturn(calendar).when(service).buildCalendarClient();

        when(calendar.events()).thenReturn(events);
    }

    // -------------------------------------------------------------------------
    // createEventWithMeet(summary, desc, attendeeEmail, start, end, timeZone)
    // -------------------------------------------------------------------------

    @Test
    void createEventWithMeet_withExplicitTimeZone_andAttendee_setsConferenceAndCallsInsert() throws IOException {
        Calendar.Events.Insert insert = mock(Calendar.Events.Insert.class);

        when(events.insert(eq("primary"), any(Event.class))).thenReturn(insert);
        when(insert.setConferenceDataVersion(1)).thenReturn(insert);

        Event created = new Event().setId("evt1").setHangoutLink("https://meet.google.com/x");
        when(insert.execute()).thenReturn(created);

        ZonedDateTime start = ZonedDateTime.of(2026, 1, 10, 9, 0, 0, 0, ZoneId.of("Europe/London"));
        ZonedDateTime end   = start.plusHours(1);

        Event out = service.createEventWithMeet(
                "Session",
                "Desc",
                "client@example.com",
                start,
                end,
                "Europe/London"
        );

        assertEquals("evt1", out.getId());

        // capture inserted Event
        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(events).insert(eq("primary"), eventCaptor.capture());

        Event sent = eventCaptor.getValue();
        assertEquals("Session", sent.getSummary());
        assertEquals("Desc", sent.getDescription());

        // timezone set
        assertNotNull(sent.getStart());
        assertNotNull(sent.getEnd());
        assertEquals("Europe/London", sent.getStart().getTimeZone());
        assertEquals("Europe/London", sent.getEnd().getTimeZone());

        // attendee added
        assertNotNull(sent.getAttendees());
        assertEquals(1, sent.getAttendees().size());
        assertEquals("client@example.com", sent.getAttendees().get(0).getEmail());

        // conference data set
        assertNotNull(sent.getConferenceData());
        assertNotNull(sent.getConferenceData().getCreateRequest());
        assertNotNull(sent.getConferenceData().getCreateRequest().getRequestId());
        assertNotNull(sent.getConferenceData().getCreateRequest().getConferenceSolutionKey());
        assertEquals("hangoutsMeet", sent.getConferenceData().getCreateRequest().getConferenceSolutionKey().getType());

        verify(insert).setConferenceDataVersion(1);
        verify(insert).execute();
    }

    @Test
    void createEventWithMeet_blankAttendee_doesNotSetAttendees() throws IOException {
        Calendar.Events.Insert insert = mock(Calendar.Events.Insert.class);
        when(events.insert(eq("primary"), any(Event.class))).thenReturn(insert);
        when(insert.setConferenceDataVersion(1)).thenReturn(insert);
        when(insert.execute()).thenReturn(new Event().setId("evt2"));

        ZonedDateTime start = ZonedDateTime.now(ZoneId.of("Europe/London")).plusDays(1);
        ZonedDateTime end = start.plusMinutes(30);

        service.createEventWithMeet("S", "D", "   ", start, end, "Europe/London");

        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(events).insert(eq("primary"), eventCaptor.capture());

        Event sent = eventCaptor.getValue();
        assertNull(sent.getAttendees(), "attendees should be null/absent when attendeeEmail blank");
    }

    @Test
    void createEventWithMeet_timeZoneNull_defaultsToEuropeLondon() throws IOException {
        Calendar.Events.Insert insert = mock(Calendar.Events.Insert.class);
        when(events.insert(eq("primary"), any(Event.class))).thenReturn(insert);
        when(insert.setConferenceDataVersion(1)).thenReturn(insert);
        when(insert.execute()).thenReturn(new Event().setId("evt3"));

        ZonedDateTime start = ZonedDateTime.now(ZoneId.of("Europe/Amsterdam")).plusDays(1);
        ZonedDateTime end = start.plusHours(1);

        service.createEventWithMeet("S", "D", "a@b.com", start, end, null);

        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(events).insert(eq("primary"), eventCaptor.capture());

        Event sent = eventCaptor.getValue();
        assertEquals("Europe/London", sent.getStart().getTimeZone());
        assertEquals("Europe/London", sent.getEnd().getTimeZone());
    }

    // -------------------------------------------------------------------------
    // overload createEventWithMeet(summary, desc, attendeeEmail, start, end)
    // -------------------------------------------------------------------------

    @Test
    void createEventWithMeet_overload_usesStartZoneId() throws IOException {
        Calendar.Events.Insert insert = mock(Calendar.Events.Insert.class);
        when(events.insert(eq("primary"), any(Event.class))).thenReturn(insert);
        when(insert.setConferenceDataVersion(1)).thenReturn(insert);
        when(insert.execute()).thenReturn(new Event().setId("evt4"));

        ZonedDateTime start = ZonedDateTime.of(2026, 1, 10, 9, 0, 0, 0, ZoneId.of("Europe/Amsterdam"));
        ZonedDateTime end = start.plusMinutes(45);

        service.createEventWithMeet("S", "D", "a@b.com", start, end);

        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(events).insert(eq("primary"), eventCaptor.capture());

        Event sent = eventCaptor.getValue();
        assertEquals("Europe/Amsterdam", sent.getStart().getTimeZone());
        assertEquals("Europe/Amsterdam", sent.getEnd().getTimeZone());
    }

    // -------------------------------------------------------------------------
    // deleteEvent
    // -------------------------------------------------------------------------

    @Test
    void deleteEvent_callsCalendarDelete() throws IOException {
        Calendar.Events.Delete delete = mock(Calendar.Events.Delete.class);
        when(events.delete("primary", "evtX")).thenReturn(delete);

        service.deleteEvent("evtX");

        verify(events).delete("primary", "evtX");
        verify(delete).execute();
    }

    // -------------------------------------------------------------------------
    // updateEventTime
    // -------------------------------------------------------------------------

    @Test
    void updateEventTime_getsThenUpdatesEvent_andSetsTimezoneDefaultIfBlank() throws IOException {
        Calendar.Events.Get get = mock(Calendar.Events.Get.class);
        Calendar.Events.Update update = mock(Calendar.Events.Update.class);

        Event existing = new Event().setId("evtU");
        when(events.get("primary", "evtU")).thenReturn(get);
        when(get.execute()).thenReturn(existing);

        when(events.update(eq("primary"), eq("evtU"), any(Event.class))).thenReturn(update);
        when(update.setConferenceDataVersion(1)).thenReturn(update);

        Event updated = new Event().setId("evtU");
        when(update.execute()).thenReturn(updated);

        ZonedDateTime newStart = ZonedDateTime.of(2026, 1, 10, 10, 0, 0, 0, ZoneId.of("Europe/London"));
        ZonedDateTime newEnd = newStart.plusHours(1);

        Event out = service.updateEventTime("evtU", newStart, newEnd, "   "); // blank -> default London

        assertEquals("evtU", out.getId());

        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(events).update(eq("primary"), eq("evtU"), eventCaptor.capture());

        Event sent = eventCaptor.getValue();
        assertNotNull(sent.getStart());
        assertNotNull(sent.getEnd());
        assertEquals("Europe/London", sent.getStart().getTimeZone());
        assertEquals("Europe/London", sent.getEnd().getTimeZone());

        verify(update).setConferenceDataVersion(1);
        verify(update).execute();
    }

    // -------------------------------------------------------------------------
    // getEvent
    // -------------------------------------------------------------------------

    @Test
    void getEvent_callsCalendarGet() throws IOException {
        Calendar.Events.Get get = mock(Calendar.Events.Get.class);
        Event event = new Event().setId("evtG");

        when(events.get("primary", "evtG")).thenReturn(get);
        when(get.execute()).thenReturn(event);

        Event out = service.getEvent("evtG");

        assertEquals("evtG", out.getId());
        verify(events).get("primary", "evtG");
        verify(get).execute();
    }
}
