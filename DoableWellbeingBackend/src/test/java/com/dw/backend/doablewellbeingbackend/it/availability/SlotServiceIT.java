package com.dw.backend.doablewellbeingbackend.it.availability;

import com.dw.backend.doablewellbeingbackend.business.availability.SlotService;
import com.dw.backend.doablewellbeingbackend.domain.appointment.SlotView;
import com.dw.backend.doablewellbeingbackend.domain.enums.AppointmentStatus;
import com.dw.backend.doablewellbeingbackend.it.IntegrationTestBase;
import com.dw.backend.doablewellbeingbackend.it.TestSeed;
import com.dw.backend.doablewellbeingbackend.persistence.entity.AppointmentEntity;
import com.dw.backend.doablewellbeingbackend.persistence.entity.CoachAvailability;
import com.dw.backend.doablewellbeingbackend.persistence.impl.AppointmentRepository;
import com.dw.backend.doablewellbeingbackend.persistence.impl.CoachAvailabilityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class SlotServiceIT extends IntegrationTestBase {

    @Autowired SlotService slotService;
    @Autowired CoachAvailabilityRepository availabilityRepository;
    @Autowired AppointmentRepository appointmentRepository;
    @Autowired JdbcTemplate jdbc;

    UUID coachId;
    UUID clientId;

    @BeforeEach
    void seed() {
        TestSeed.ensureRole(jdbc, "coach");
        TestSeed.ensureRole(jdbc, "client");

        coachId = TestSeed.insertUser(jdbc, "coach_slot_it@test.com", "Coach", "Slot".getBytes(StandardCharsets.UTF_8), "John", "Doe");
        clientId = TestSeed.insertUser(jdbc, "client_slot_it@test.com", "Client", "Slot".getBytes(StandardCharsets.UTF_8), "John", "Doe");

        TestSeed.assignRole(jdbc, coachId, "coach");
        TestSeed.assignRole(jdbc, clientId, "client");

        TestSeed.insertCoach(jdbc, coachId);
        TestSeed.insertClient(jdbc, clientId);
    }

    @Test
    void getSlots_generatesFromAvailability_whenNoAppointments() {
        LocalDate d = LocalDate.now().plusDays(5);

        availabilityRepository.save(CoachAvailability.builder()
                .coachId(coachId)
                .date(d)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(11, 0))
                .isActive(true)
                .isRecurring(false)
                .build());

        ZoneId coachZone = ZoneId.of("Europe/London");

        List<SlotView> slots = slotService.getSlotsForCoach(
                coachId,
                d, d,
                60,
                coachZone
        );

        assertThat(slots).hasSize(2);
        assertThat(slots.get(0).getStartTime()).isEqualTo(LocalTime.of(9, 0));
        assertThat(slots.get(1).getStartTime()).isEqualTo(LocalTime.of(10, 0));
    }

    @Test
    void getSlots_excludesOverlappingAppointments() {
        LocalDate d = LocalDate.now().plusDays(6);

        availabilityRepository.save(CoachAvailability.builder()
                .coachId(coachId)
                .date(d)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(12, 0))
                .isActive(true)
                .isRecurring(false)
                .build());

        // Appointment: 10:00-11:00 -> a 10:00 slot kiesik
        ZoneId coachZone = ZoneId.of("Europe/London");
        ZoneOffset offset = coachZone.getRules().getOffset(Instant.now());

        OffsetDateTime apptStart = OffsetDateTime.of(d, LocalTime.of(10, 0), offset);
        OffsetDateTime apptEnd = OffsetDateTime.of(d, LocalTime.of(11, 0), offset);

        appointmentRepository.save(AppointmentEntity.builder()
                .coachId(coachId)
                .clientId(clientId)
                .startsAt(apptStart)
                .endsAt(apptEnd)
                .status(AppointmentStatus.scheduled)
                .build());

        List<SlotView> slots = slotService.getSlotsForCoach(
                coachId, d, d, 60, coachZone
        );

        // 9-10, 10-11, 11-12 lenne -> a középső kiesik
        assertThat(slots).hasSize(2);
        assertThat(slots).extracting(SlotView::getStartTime)
                .containsExactly(LocalTime.of(9, 0), LocalTime.of(11, 0));
    }

    @Test
    void getSlots_ignoresInactiveAvailabilities() {
        LocalDate d = LocalDate.now().plusDays(7);

        availabilityRepository.save(CoachAvailability.builder()
                .coachId(coachId)
                .date(d)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(11, 0))
                .isActive(false)
                .isRecurring(false)
                .build());

        List<SlotView> slots = slotService.getSlotsForCoach(
                coachId, d, d, 60, ZoneId.of("Europe/London")
        );

        assertThat(slots).isEmpty();
    }

}
