package com.dw.backend.doablewellbeingbackend.it.appointment;

import com.dw.backend.doablewellbeingbackend.business.appointment.AppointmentService;
import com.dw.backend.doablewellbeingbackend.business.google.GoogleCalendarService;
import com.dw.backend.doablewellbeingbackend.domain.enums.AppointmentStatus;
import com.dw.backend.doablewellbeingbackend.it.IntegrationTestBase;
import com.dw.backend.doablewellbeingbackend.it.TestSeed;
import com.dw.backend.doablewellbeingbackend.persistence.entity.AppointmentEntity;
import com.dw.backend.doablewellbeingbackend.persistence.impl.AppointmentRepository;
import com.google.api.services.calendar.model.Event;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class AppointmentServiceIT extends IntegrationTestBase {

    @Autowired AppointmentService appointmentService;
    @Autowired AppointmentRepository appointmentRepository;
    @Autowired JdbcTemplate jdbc;

    @MockitoBean
    GoogleCalendarService googleCalendarService;

    UUID coachId;
    UUID clientId;

    @BeforeEach
    void seed() {
        // roles
        TestSeed.ensureRole(jdbc, "coach");
        TestSeed.ensureRole(jdbc, "user");
        TestSeed.ensureRole(jdbc, "client");

        // users
        coachId = TestSeed.insertUser(jdbc, "coach@test.com", "Coach", "Two".getBytes(StandardCharsets.UTF_8), "John", "Doe");
        clientId = TestSeed.insertUser(jdbc, "client@test.com", "Client", "One".getBytes(StandardCharsets.UTF_8), "John", "Doe");

        // role mapping + coach/client tables
        TestSeed.assignRole(jdbc, coachId, "coach");
        TestSeed.assignRole(jdbc, clientId, "client");

        TestSeed.insertCoach(jdbc, coachId);
        TestSeed.insertClient(jdbc, clientId);

        reset(googleCalendarService);
    }

    @Test
    void requestAppointment_firstSession_createsRequested_noMeet() {
        OffsetDateTime start = OffsetDateTime.now().plusDays(2).withMinute(0).withSecond(0).withNano(0);

        AppointmentEntity appt = appointmentService.requestAppointmentFromSlot(
                coachId, clientId, start, 60, "hi"
        );

        assertThat(appt.getId()).isNotNull();
        assertThat(appt.getStatus()).isEqualTo(AppointmentStatus.requested);

        assertThat(appt.getExternalCalendarId()).isNull();
        assertThat(appt.getMeetingUrl()).isNull();

        verifyNoInteractions(googleCalendarService);
    }

    @Test
    void requestAppointment_existingClient_createsScheduled_andMeet() throws Exception {
        // Seed: completed appointment earlier -> COUNT_AS_EXISTING includes completed
        AppointmentEntity past = AppointmentEntity.builder()
                .coachId(coachId)
                .clientId(clientId)
                .startsAt(OffsetDateTime.now().minusDays(10))
                .endsAt(OffsetDateTime.now().minusDays(10).plusMinutes(60))
                .status(AppointmentStatus.completed)
                .build();
        appointmentRepository.save(past);

        Event event = new Event();
        event.setId("evt-1");
        event.setHangoutLink("https://meet.google.com/abc-defg-hij");

        when(googleCalendarService.createEventWithMeet(any(), any(), any(), any(), any()))
                .thenReturn(event);

        OffsetDateTime start = OffsetDateTime.now().plusDays(3).withMinute(0).withSecond(0).withNano(0);

        AppointmentEntity appt = appointmentService.requestAppointmentFromSlot(
                coachId, clientId, start, 60, "notes"
        );

        assertThat(appt.getStatus()).isEqualTo(AppointmentStatus.scheduled);
        assertThat(appt.getConfirmedAt()).isNotNull();
        assertThat(appt.getExternalCalendarId()).isEqualTo("evt-1");
        assertThat(appt.getMeetingUrl()).contains("meet.google.com");

        verify(googleCalendarService, times(1))
                .createEventWithMeet(any(), any(), eq("client@test.com"), any(), any());
    }

    @Test
    void confirmAppointment_requested_becomesScheduled_andMeet() throws Exception {
        // create requested appointment
        AppointmentEntity req = AppointmentEntity.builder()
                .coachId(coachId)
                .clientId(clientId)
                .startsAt(OffsetDateTime.now().plusDays(4))
                .endsAt(OffsetDateTime.now().plusDays(4).plusMinutes(60))
                .status(AppointmentStatus.requested)
                .notes("hello")
                .build();
        req = appointmentRepository.save(req);

        Event event = new Event();
        event.setId("evt-2");
        event.setHangoutLink("https://meet.google.com/xxx-yyyy-zzz");
        when(googleCalendarService.createEventWithMeet(any(), any(), any(), any(), any()))
                .thenReturn(event);

        AppointmentEntity updated = appointmentService.confirmAppointment(coachId, req.getId());

        assertThat(updated.getStatus()).isEqualTo(AppointmentStatus.scheduled);
        assertThat(updated.getConfirmedAt()).isNotNull();
        assertThat(updated.getMeetingUrl()).isEqualTo("https://meet.google.com/xxx-yyyy-zzz");

        verify(googleCalendarService, times(1)).createEventWithMeet(any(), any(), any(), any(), any());
    }

    @Test
    void cancelAppointment_client_onlyFuture() {
        AppointmentEntity future = AppointmentEntity.builder()
                .coachId(coachId)
                .clientId(clientId)
                .startsAt(OffsetDateTime.now().plusDays(5))
                .endsAt(OffsetDateTime.now().plusDays(5).plusMinutes(60))
                .status(AppointmentStatus.scheduled)
                .build();
        future = appointmentRepository.save(future);

        AppointmentEntity cancelled = appointmentService.cancelAppointmentAsClient(clientId, future.getId());
        assertThat(cancelled.getStatus()).isEqualTo(AppointmentStatus.cancelled);

        AppointmentEntity past = AppointmentEntity.builder()
                .coachId(coachId)
                .clientId(clientId)
                .startsAt(OffsetDateTime.now().minusDays(1))
                .endsAt(OffsetDateTime.now().minusDays(1).plusMinutes(60))
                .status(AppointmentStatus.scheduled)
                .build();
        past = appointmentRepository.save(past);

        UUID pastId = past.getId();
        assertThatThrownBy(() -> appointmentService.cancelAppointmentAsClient(clientId, pastId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void repository_existsOverlap_works() {
        OffsetDateTime start = OffsetDateTime.now().plusDays(7);
        OffsetDateTime end = start.plusMinutes(60);

        AppointmentEntity a = AppointmentEntity.builder()
                .coachId(coachId)
                .clientId(clientId)
                .startsAt(start)
                .endsAt(end)
                .status(AppointmentStatus.scheduled)
                .build();
        appointmentRepository.save(a);

        boolean overlaps = appointmentRepository.existsOverlap(coachId, start.plusMinutes(15), end.plusMinutes(15));
        assertThat(overlaps).isTrue();

        boolean noOverlap = appointmentRepository.existsOverlap(coachId, end.plusMinutes(1), end.plusMinutes(30));
        assertThat(noOverlap).isFalse();
    }

    @Test
    void getUpcomingForClient_filtersCorrectly() {
        OffsetDateTime from = OffsetDateTime.now();
        OffsetDateTime to = from.plusDays(14);

        // upcoming
        appointmentRepository.save(AppointmentEntity.builder()
                .coachId(coachId).clientId(clientId)
                .startsAt(from.plusDays(2)).endsAt(from.plusDays(2).plusMinutes(60))
                .status(AppointmentStatus.scheduled)
                .build());

        // past
        appointmentRepository.save(AppointmentEntity.builder()
                .coachId(coachId).clientId(clientId)
                .startsAt(from.minusDays(2)).endsAt(from.minusDays(2).plusMinutes(60))
                .status(AppointmentStatus.scheduled)
                .build());

        List<AppointmentEntity> res = appointmentRepository.findUpcomingForClient(clientId, from, to);
        assertThat(res).allMatch(a -> !a.getStartsAt().isBefore(from));
    }
}
