package com.dw.backend.doablewellbeingbackend.business.appointment;

import com.dw.backend.doablewellbeingbackend.business.google.GoogleCalendarService;
import com.dw.backend.doablewellbeingbackend.common.exception.AccessDeniedException;
import com.dw.backend.doablewellbeingbackend.common.exception.NotFoundException;
import com.dw.backend.doablewellbeingbackend.domain.coach.ClientSummaryView;
import com.dw.backend.doablewellbeingbackend.domain.coach.CoachCalendarEventView;
import com.dw.backend.doablewellbeingbackend.domain.dashboard.AddAppointmentNoteRequest;
import com.dw.backend.doablewellbeingbackend.domain.dashboard.AddAppointmentResourceRequest;
import com.dw.backend.doablewellbeingbackend.domain.enums.AppointmentStatus;
import com.dw.backend.doablewellbeingbackend.persistence.entity.AppointmentEntity;
import com.dw.backend.doablewellbeingbackend.persistence.entity.AppointmentNoteEntity;
import com.dw.backend.doablewellbeingbackend.persistence.entity.AppointmentResourceEntity;
import com.dw.backend.doablewellbeingbackend.persistence.entity.CoachEntity;
import com.dw.backend.doablewellbeingbackend.persistence.entity.UserEntity;
import com.dw.backend.doablewellbeingbackend.persistence.impl.*;
import com.google.api.services.calendar.model.Event;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceImplTest {

    @Mock AppointmentRepository apptRepo;
    @Mock CoachRepository coachRepo;
    @Mock UserRepository userRepo;
    @Mock GoogleCalendarService googleCalendarService;
    @Mock AppointmentNoteRepository appointmentNoteRepository;
    @Mock AppointmentResourceRepository appointmentResourceRepository;

    @InjectMocks AppointmentServiceImpl service;

    private final UUID coachId = UUID.randomUUID();
    private final UUID clientId = UUID.randomUUID();
    private final UUID apptId = UUID.randomUUID();

    private OffsetDateTime start;
    private OffsetDateTime end;

    @BeforeEach
    void setup() {

        start = OffsetDateTime.now().plusDays(2).withSecond(0).withNano(0);
        end = start.plusMinutes(60);
    }

    // -------------------------------------------------------------------------
    // REQUEST APPOINTMENT FROM SLOT
    // -------------------------------------------------------------------------

    @Test
    void requestAppointmentFromSlot_invalidDuration_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () ->
                service.requestAppointmentFromSlot(coachId, clientId, start, 17, "notes")
        );
        verifyNoInteractions(apptRepo, coachRepo, userRepo, googleCalendarService);
    }

    @Test
    void requestAppointmentFromSlot_coachNotFound_throwsNotFound() {
        when(coachRepo.findById(coachId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->
                service.requestAppointmentFromSlot(coachId, clientId, start, 60, "x")
        );

        verifyNoInteractions(apptRepo, googleCalendarService);
    }

    @Test
    void requestAppointmentFromSlot_clientNotFound_throwsNotFound() {
        when(coachRepo.findById(coachId)).thenReturn(Optional.of(coachEntity(coachId)));
        when(userRepo.findById(clientId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->
                service.requestAppointmentFromSlot(coachId, clientId, start, 60, "x")
        );

        verifyNoInteractions(apptRepo, googleCalendarService);
    }

    @Test
    void requestAppointmentFromSlot_firstSession_createsRequested_noGoogleMeet() {
        CoachEntity coach = coachEntity(coachId);
        UserEntity client = clientEmailOnly(clientId);

        when(coachRepo.findById(coachId)).thenReturn(Optional.of(coach));
        when(userRepo.findById(clientId)).thenReturn(Optional.of(client));
        when(apptRepo.existsOverlap(eq(coachId), any(), any())).thenReturn(false);
        when(apptRepo.existsByCoachIdAndClientIdAndStatusIn(eq(coachId), eq(clientId), anyList()))
                .thenReturn(false);

        when(apptRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AppointmentEntity created =
                service.requestAppointmentFromSlot(coachId, clientId, start, 60, "hello");

        assertEquals(AppointmentStatus.requested, created.getStatus());
        assertNull(created.getConfirmedAt());

        verifyNoInteractions(googleCalendarService);
    }

    @Test
    void requestAppointmentFromSlot_existingRelationship_createsScheduled_andGoogleMeetAttached() throws Exception {
        CoachEntity coach = coachEntity(coachId);
        UserEntity client = clientFull(clientId);

        when(coachRepo.findById(coachId)).thenReturn(Optional.of(coach));
        when(userRepo.findById(clientId)).thenReturn(Optional.of(client));
        when(apptRepo.existsOverlap(eq(coachId), any(), any())).thenReturn(false);
        when(apptRepo.existsByCoachIdAndClientIdAndStatusIn(eq(coachId), eq(clientId), anyList()))
                .thenReturn(true);

        Event event = new Event().setId("evt_1").setHangoutLink("https://meet.google.com/abc");
        when(googleCalendarService.createEventWithMeet(any(), any(), any(), any(), any()))
                .thenReturn(event);

        when(apptRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AppointmentEntity created =
                service.requestAppointmentFromSlot(coachId, clientId, start, 60, "note");

        assertEquals(AppointmentStatus.scheduled, created.getStatus());
        assertNotNull(created.getConfirmedAt());
        assertEquals("evt_1", created.getExternalCalendarId());
        assertEquals("google", created.getExternalCalendarProvider());
        assertEquals("https://meet.google.com/abc", created.getMeetingUrl());

        verify(googleCalendarService).createEventWithMeet(
                anyString(),
                anyString(),
                eq("client@example.com"),
                any(),
                any()
        );
    }

    @Test
    void requestAppointmentFromSlot_overlap_throwsIllegalState() {
        when(coachRepo.findById(coachId)).thenReturn(Optional.of(coachEntity(coachId)));
        when(userRepo.findById(clientId)).thenReturn(Optional.of(clientEmailOnly(clientId)));
        when(apptRepo.existsOverlap(eq(coachId), any(), any())).thenReturn(true);

        assertThrows(IllegalStateException.class, () ->
                service.requestAppointmentFromSlot(coachId, clientId, start, 60, null)
        );

        verify(apptRepo, never()).save(any());
        verifyNoInteractions(googleCalendarService);
    }

    // -------------------------------------------------------------------------
    // CONFIRM
    // -------------------------------------------------------------------------

    @Test
    void confirmAppointment_notFound_throwsNotFound() {
        when(apptRepo.findById(apptId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.confirmAppointment(coachId, apptId));
        verify(apptRepo, never()).save(any());
    }

    @Test
    void confirmAppointment_wrongCoach_throwsAccessDenied() {
        AppointmentEntity appt = AppointmentEntity.builder()
                .id(apptId)
                .coachId(UUID.randomUUID())
                .clientId(clientId)
                .startsAt(start)
                .endsAt(end)
                .status(AppointmentStatus.requested)
                .build();

        when(apptRepo.findById(apptId)).thenReturn(Optional.of(appt));

        assertThrows(AccessDeniedException.class, () -> service.confirmAppointment(coachId, apptId));
        verify(apptRepo, never()).save(any());
    }

    @Test
    void confirmAppointment_wrongStatus_throwsIllegalState() {
        AppointmentEntity appt = AppointmentEntity.builder()
                .id(apptId)
                .coachId(coachId)
                .clientId(clientId)
                .startsAt(start)
                .endsAt(end)
                .status(AppointmentStatus.scheduled)
                .build();

        when(apptRepo.findById(apptId)).thenReturn(Optional.of(appt));

        assertThrows(IllegalStateException.class, () -> service.confirmAppointment(coachId, apptId));
        verify(apptRepo, never()).save(any());
    }

    @Test
    void confirmAppointment_clientNotFound_throwsNotFound_andDoesNotSave() {
        AppointmentEntity appt = AppointmentEntity.builder()
                .id(apptId)
                .coachId(coachId)
                .clientId(clientId)
                .startsAt(start)
                .endsAt(end)
                .status(AppointmentStatus.requested)
                .build();

        when(apptRepo.findById(apptId)).thenReturn(Optional.of(appt));
        when(userRepo.findById(clientId)).thenReturn(Optional.empty()); // client hiányzik

        assertThrows(NotFoundException.class, () ->
                service.confirmAppointment(coachId, apptId)
        );

        verify(apptRepo, never()).save(any());
        verifyNoInteractions(googleCalendarService);
    }

    @Test
    void confirmAppointment_googleMeetFails_wrapsRuntimeException() throws Exception {
        AppointmentEntity appt = AppointmentEntity.builder()
                .id(apptId)
                .coachId(coachId)
                .clientId(clientId)
                .startsAt(start)
                .endsAt(end)
                .status(AppointmentStatus.requested)
                .build();

        when(apptRepo.findById(apptId)).thenReturn(Optional.of(appt));

        UserEntity client = mock(UserEntity.class);
        when(client.getEmail()).thenReturn("client@example.com");
        when(userRepo.findById(clientId)).thenReturn(Optional.of(client));

        when(googleCalendarService.createEventWithMeet(any(), any(), any(), any(), any()))
                .thenThrow(new IOException("boom"));

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                service.confirmAppointment(coachId, apptId)
        );
        assertTrue(ex.getMessage().contains("Failed to create Google Meet event"));

        verify(apptRepo, never()).save(any());
    }

    @Test
    void confirmAppointment_success_setsScheduled_confirmedAt_andEventFields() throws Exception {
        AppointmentEntity appt = AppointmentEntity.builder()
                .id(apptId)
                .coachId(coachId)
                .clientId(clientId)
                .startsAt(start)
                .endsAt(end)
                .status(AppointmentStatus.requested)
                .notes("note")
                .build();

        UserEntity client = mock(UserEntity.class);
        when(client.getEmail()).thenReturn("client@example.com");

        when(apptRepo.findById(apptId)).thenReturn(Optional.of(appt));
        when(userRepo.findById(clientId)).thenReturn(Optional.of(client));

        Event event = new Event().setId("evt_confirm").setHangoutLink("https://meet.google.com/confirm");
        when(googleCalendarService.createEventWithMeet(any(), any(), any(), any(), any()))
                .thenReturn(event);

        when(apptRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AppointmentEntity saved = service.confirmAppointment(coachId, apptId);

        assertEquals(AppointmentStatus.scheduled, saved.getStatus());
        assertNotNull(saved.getConfirmedAt());
        assertEquals("evt_confirm", saved.getExternalCalendarId());
        assertEquals("google", saved.getExternalCalendarProvider());
        assertEquals("https://meet.google.com/confirm", saved.getMeetingUrl());

        verify(apptRepo).save(appt);
    }

    // -------------------------------------------------------------------------
    // COMPLETE
    // -------------------------------------------------------------------------

    @Test
    void completeAppointment_notFound_throwsNotFound() {
        when(apptRepo.findById(apptId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.completeAppointment(coachId, apptId));
        verify(apptRepo, never()).save(any());
    }

    @Test
    void completeAppointment_wrongCoach_throwsAccessDenied() {
        AppointmentEntity appt = AppointmentEntity.builder()
                .id(apptId)
                .coachId(UUID.randomUUID())
                .status(AppointmentStatus.scheduled)
                .build();

        when(apptRepo.findById(apptId)).thenReturn(Optional.of(appt));

        assertThrows(AccessDeniedException.class, () -> service.completeAppointment(coachId, apptId));
        verify(apptRepo, never()).save(any());
    }

    @Test
    void completeAppointment_wrongStatus_throwsIllegalState() {
        AppointmentEntity appt = AppointmentEntity.builder()
                .id(apptId)
                .coachId(coachId)
                .status(AppointmentStatus.requested)
                .build();

        when(apptRepo.findById(apptId)).thenReturn(Optional.of(appt));

        assertThrows(IllegalStateException.class, () -> service.completeAppointment(coachId, apptId));
        verify(apptRepo, never()).save(any());
    }

    @Test
    void completeAppointment_success_setsCompleted() {
        AppointmentEntity appt = AppointmentEntity.builder()
                .id(apptId)
                .coachId(coachId)
                .status(AppointmentStatus.scheduled)
                .build();

        when(apptRepo.findById(apptId)).thenReturn(Optional.of(appt));
        when(apptRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AppointmentEntity out = service.completeAppointment(coachId, apptId);

        assertEquals(AppointmentStatus.completed, out.getStatus());
        verify(apptRepo).save(appt);
    }

    // -------------------------------------------------------------------------
    // DECLINE
    // -------------------------------------------------------------------------

    @Test
    void declineAppointment_success_setsDeclined() {
        AppointmentEntity appt = AppointmentEntity.builder()
                .id(apptId)
                .coachId(coachId)
                .clientId(clientId)
                .startsAt(start)
                .endsAt(end)
                .status(AppointmentStatus.requested)
                .build();

        when(apptRepo.findById(apptId)).thenReturn(Optional.of(appt));
        when(apptRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AppointmentEntity saved = service.declineAppointment(coachId, apptId, "nope");

        assertEquals(AppointmentStatus.declined, saved.getStatus());
        verify(apptRepo).save(appt);
        verifyNoInteractions(googleCalendarService);
    }

    // -------------------------------------------------------------------------
    // CANCEL (client/coach)
    // -------------------------------------------------------------------------

    @Test
    void cancelAppointmentAsClient_notOwner_throwsAccessDenied() {
        AppointmentEntity appt = AppointmentEntity.builder()
                .id(apptId)
                .coachId(coachId)
                .clientId(UUID.randomUUID())
                .startsAt(start)
                .endsAt(end)
                .status(AppointmentStatus.scheduled)
                .build();

        when(apptRepo.findById(apptId)).thenReturn(Optional.of(appt));

        assertThrows(AccessDeniedException.class, () ->
                service.cancelAppointmentAsClient(clientId, apptId)
        );
        verify(apptRepo, never()).save(any());
    }

    @Test
    void cancelAppointmentAsClient_pastStartOrNotCancelable_throwsIllegalState() {
        AppointmentEntity appt = AppointmentEntity.builder()
                .id(apptId)
                .coachId(coachId)
                .clientId(clientId)
                .startsAt(OffsetDateTime.now().minusHours(1))
                .endsAt(OffsetDateTime.now().plusHours(1))
                .status(AppointmentStatus.scheduled)
                .build();

        when(apptRepo.findById(apptId)).thenReturn(Optional.of(appt));

        assertThrows(IllegalStateException.class, () ->
                service.cancelAppointmentAsClient(clientId, apptId)
        );
        verify(apptRepo, never()).save(any());
    }

    @Test
    void cancelAppointmentAsCoach_success_setsCancelled() {
        AppointmentEntity appt = AppointmentEntity.builder()
                .id(apptId)
                .coachId(coachId)
                .clientId(clientId)
                .startsAt(start)
                .endsAt(end)
                .status(AppointmentStatus.scheduled)
                .build();

        when(apptRepo.findById(apptId)).thenReturn(Optional.of(appt));
        when(apptRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AppointmentEntity saved = service.cancelAppointmentAsCoach(coachId, apptId);

        assertEquals(AppointmentStatus.cancelled, saved.getStatus());
        verify(apptRepo).save(appt);
    }

    // -------------------------------------------------------------------------
    // INSTANT BOOKING
    // -------------------------------------------------------------------------

    @Test
    void instantBookFromSlot_invalidDuration_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () ->
                service.instantBookFromSlot(coachId, clientId, start, 999, null)
        );
        verifyNoInteractions(apptRepo, coachRepo, userRepo, googleCalendarService);
    }

    @Test
    void instantBookFromSlot_coachNotFound_throwsNotFound() {
        when(coachRepo.findById(coachId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->
                service.instantBookFromSlot(coachId, clientId, start, 60, "x")
        );
        verifyNoInteractions(apptRepo, googleCalendarService);
    }

    @Test
    void instantBookFromSlot_clientNotFound_throwsNotFound() {
        when(coachRepo.findById(coachId)).thenReturn(Optional.of(coachEntity(coachId)));
        when(userRepo.findById(clientId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.instantBookFromSlot(coachId, clientId, start, 60, "x"));
        verifyNoInteractions(apptRepo, googleCalendarService);
    }

    @Test
    void instantBookFromSlot_overlap_throwsIllegalState() {
        when(coachRepo.findById(coachId)).thenReturn(Optional.of(coachEntity(coachId)));
        when(userRepo.findById(clientId)).thenReturn(Optional.of(clientEmailOnly(clientId)));

        when(apptRepo.existsOverlap(eq(coachId), any(), any())).thenReturn(true);

        assertThrows(IllegalStateException.class, () ->
                service.instantBookFromSlot(coachId, clientId, start, 60, "x")
        );

        verify(apptRepo, never()).save(any());
        verifyNoInteractions(googleCalendarService);
    }

    @Test
    void instantBookFromSlot_success_scheduled_confirmed_googleAttached() throws Exception {
        var coachEntity = coachEntity(coachId);
        var clientEntity = clientFull(clientId);

        when(coachRepo.findById(coachId)).thenReturn(Optional.of(coachEntity));
        when(userRepo.findById(clientId)).thenReturn(Optional.of(clientEntity));
        when(apptRepo.existsOverlap(eq(coachId), any(), any())).thenReturn(false);

        Event event = new Event().setId("evt_inst").setHangoutLink("https://meet.google.com/inst");
        when(googleCalendarService.createEventWithMeet(any(), any(), any(), any(), any()))
                .thenReturn(event);

        when(apptRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AppointmentEntity saved = service.instantBookFromSlot(coachId, clientId, start, 60, "yo");

        assertEquals(AppointmentStatus.scheduled, saved.getStatus());
        assertNotNull(saved.getConfirmedAt());
        assertEquals("evt_inst", saved.getExternalCalendarId());
        assertEquals("https://meet.google.com/inst", saved.getMeetingUrl());
        verify(apptRepo).save(any(AppointmentEntity.class));
    }

    // -------------------------------------------------------------------------
    // GETTERS: calendar mapping + previewNotes
    // -------------------------------------------------------------------------

    @Test
    void getCoachCalendar_mapsProjection_andPreviewsNotes() {
        var p = mock(CoachCalendarProjection.class);
        UUID apptId2 = UUID.randomUUID();

        when(p.getId()).thenReturn(apptId2);
        when(p.getClientId()).thenReturn(clientId);
        when(p.getClientName()).thenReturn("John Doe");
        when(p.getClientEmail()).thenReturn("john@doe.com");
        when(p.getStartsAt()).thenReturn(start);
        when(p.getEndsAt()).thenReturn(end);
        when(p.getStatus()).thenReturn(AppointmentStatus.scheduled);
        when(p.getMeetingUrl()).thenReturn("https://meet.google.com/x");
        when(p.getExternalCalendarId()).thenReturn("evt_x");
        when(p.getNotes()).thenReturn("a".repeat(200));

        when(apptRepo.findCoachCalendar(eq(coachId), any(), any()))
                .thenReturn(List.of(p));

        List<CoachCalendarEventView> out =
                service.getCoachCalendar(coachId, start.minusDays(1), end.plusDays(1));

        assertEquals(1, out.size());
        CoachCalendarEventView v = out.get(0);

        assertEquals(apptId2, v.id());
        assertEquals("John Doe", v.client().name());
        assertEquals("john@doe.com", v.client().email());

        assertNotNull(v.notesPreview());
        assertTrue(v.notesPreview().length() <= 121); // 120 + "…"
        assertTrue(v.notesPreview().endsWith("…"));
    }

    @Test
    void getCoachCalendar_previewNotes_nullOrBlank_returnsNull() {
        var p = mock(CoachCalendarProjection.class);
        when(p.getId()).thenReturn(UUID.randomUUID());
        when(p.getClientId()).thenReturn(clientId);
        when(p.getClientName()).thenReturn("John Doe");
        when(p.getClientEmail()).thenReturn("john@doe.com");
        when(p.getStartsAt()).thenReturn(start);
        when(p.getEndsAt()).thenReturn(end);
        when(p.getStatus()).thenReturn(AppointmentStatus.scheduled);
        when(p.getNotes()).thenReturn("   "); // blank

        when(apptRepo.findCoachCalendar(eq(coachId), any(), any()))
                .thenReturn(List.of(p));

        var out = service.getCoachCalendar(coachId, start.minusDays(1), end.plusDays(1));
        assertEquals(1, out.size());
        assertNull(out.get(0).notesPreview());
    }

    @Test
    void getCoachCalendar_previewNotes_short_keptAsIs() {
        var p = mock(CoachCalendarProjection.class);
        String txt = "short notes";

        when(p.getId()).thenReturn(UUID.randomUUID());
        when(p.getClientId()).thenReturn(clientId);
        when(p.getClientName()).thenReturn("John Doe");
        when(p.getClientEmail()).thenReturn("john@doe.com");
        when(p.getStartsAt()).thenReturn(start);
        when(p.getEndsAt()).thenReturn(end);
        when(p.getStatus()).thenReturn(AppointmentStatus.scheduled);
        when(p.getNotes()).thenReturn(txt);

        when(apptRepo.findCoachCalendar(eq(coachId), any(), any()))
                .thenReturn(List.of(p));

        var out = service.getCoachCalendar(coachId, start.minusDays(1), end.plusDays(1));
        assertEquals(1, out.size());
        assertEquals(txt, out.get(0).notesPreview());
    }

    // -------------------------------------------------------------------------
    // ADD NOTE / ADD RESOURCE
    // -------------------------------------------------------------------------

    @Test
    void addNote_apptNotFound_throws() {
        when(apptRepo.findById(apptId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                service.addNote(coachId, apptId, new AddAppointmentNoteRequest("hi"))
        );

        verifyNoInteractions(appointmentNoteRepository);
    }

    @Test
    void addNote_wrongCoach_throws() {
        AppointmentEntity appt = AppointmentEntity.builder()
                .id(apptId)
                .coachId(UUID.randomUUID())
                .build();

        when(apptRepo.findById(apptId)).thenReturn(Optional.of(appt));

        assertThrows(IllegalArgumentException.class, () ->
                service.addNote(coachId, apptId, new AddAppointmentNoteRequest("hi"))
        );

        verifyNoInteractions(appointmentNoteRepository);
    }

    @Test
    void addNote_success_savesNoteEntity() {
        AppointmentEntity appt = AppointmentEntity.builder()
                .id(apptId)
                .coachId(coachId)
                .build();

        when(apptRepo.findById(apptId)).thenReturn(Optional.of(appt));

        ArgumentCaptor<AppointmentNoteEntity> captor = ArgumentCaptor.forClass(AppointmentNoteEntity.class);

        service.addNote(coachId, apptId, new AddAppointmentNoteRequest("note text"));

        verify(appointmentNoteRepository).save(captor.capture());

        AppointmentNoteEntity saved = captor.getValue();
        assertEquals(apptId, saved.getAppointmentId());
        assertEquals(coachId, saved.getCreatedBy());
        assertEquals("note text", saved.getNote());
        assertNotNull(saved.getCreatedAt());
    }

    @Test
    void addResource_apptNotFound_throws() {
        when(apptRepo.findById(apptId)).thenReturn(Optional.empty());

        AddAppointmentResourceRequest req = new AddAppointmentResourceRequest(
                "file.pdf", 123L, "application/pdf", "https://example.com/file.pdf"
        );

        assertThrows(IllegalArgumentException.class, () ->
                service.addResource(coachId, apptId, req)
        );

        verifyNoInteractions(appointmentResourceRepository);
    }

    @Test
    void addResource_wrongCoach_throws() {
        AppointmentEntity appt = AppointmentEntity.builder()
                .id(apptId)
                .coachId(UUID.randomUUID())
                .build();

        when(apptRepo.findById(apptId)).thenReturn(Optional.of(appt));

        AddAppointmentResourceRequest req = new AddAppointmentResourceRequest(
                "file.pdf", 123L, "application/pdf", "https://example.com/file.pdf"
        );

        assertThrows(IllegalArgumentException.class, () ->
                service.addResource(coachId, apptId, req)
        );

        verifyNoInteractions(appointmentResourceRepository);
    }

    @Test
    void addResource_success_returnsSavedId_andPersistsFields() {
        AppointmentEntity appt = AppointmentEntity.builder()
                .id(apptId)
                .coachId(coachId)
                .build();

        when(apptRepo.findById(apptId)).thenReturn(Optional.of(appt));

        UUID resourceId = UUID.randomUUID();

        // ✅ return value: úgy viselkedik, mint DB (visszaad egy entity-t id-val)
        when(appointmentResourceRepository.save(any())).thenAnswer(inv -> {
            AppointmentResourceEntity in = inv.getArgument(0);

            // Ha van builder id-val: return AppointmentResourceEntity.builder().id(resourceId)....
            // Ha van setter: in.setId(resourceId); return in;
            // Itt builderes példát mutatok:
            return AppointmentResourceEntity.builder()
                    .id(resourceId)
                    .appointmentId(in.getAppointmentId())
                    .uploadedBy(in.getUploadedBy())
                    .fileName(in.getFileName())
                    .sizeBytes(in.getSizeBytes())
                    .mimeType(in.getMimeType())
                    .url(in.getUrl())
                    .createdAt(in.getCreatedAt())
                    .build();
        });

        AddAppointmentResourceRequest req = new AddAppointmentResourceRequest(
                "file.pdf", 123L, "application/pdf", "https://example.com/file.pdf"
        );

        UUID out = service.addResource(coachId, apptId, req);
        assertEquals(resourceId, out);

        ArgumentCaptor<AppointmentResourceEntity> captor = ArgumentCaptor.forClass(AppointmentResourceEntity.class);
        verify(appointmentResourceRepository).save(captor.capture());

        AppointmentResourceEntity saved = captor.getValue();
        assertEquals(apptId, saved.getAppointmentId());
        assertEquals(coachId, saved.getUploadedBy());
        assertEquals("file.pdf", saved.getFileName());
        assertEquals(123L, saved.getSizeBytes());
        assertEquals("application/pdf", saved.getMimeType());
        assertEquals("https://example.com/file.pdf", saved.getUrl());
        assertNotNull(saved.getCreatedAt());
    }


    // -------------------------------------------------------------------------
    // Helpers (FIXED: no entity mocking + no unnecessary stubbing)
    // -------------------------------------------------------------------------


    private static CoachEntity coachEntity(UUID coachUserId) {
        CoachEntity coach = new CoachEntity();
        coach.setUserId(coachUserId);
        return coach;
    }
    private static UserEntity clientIdOnly(UUID clientId) {
        UserEntity u = new UserEntity();
        u.setId(clientId);
        return u;
    }

    private static UserEntity clientEmailOnly(UUID clientId) {
        UserEntity u = new UserEntity();
        u.setId(clientId);
        u.setEmail("client@example.com");
        return u;
    }

    private static UserEntity clientFull(UUID clientId) {
        UserEntity u = new UserEntity();
        u.setId(clientId);
        u.setEmail("client@example.com");
        u.setFirstName("Jane");
        u.setLastName("Client");
        return u;
    }


}
