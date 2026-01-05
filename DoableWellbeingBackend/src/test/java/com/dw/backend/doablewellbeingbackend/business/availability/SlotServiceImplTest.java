package com.dw.backend.doablewellbeingbackend.business.availability;

import com.dw.backend.doablewellbeingbackend.domain.appointment.SlotView;
import com.dw.backend.doablewellbeingbackend.persistence.entity.AppointmentEntity;
import com.dw.backend.doablewellbeingbackend.persistence.entity.CoachAvailability;
import com.dw.backend.doablewellbeingbackend.persistence.impl.AppointmentRepository;
import com.dw.backend.doablewellbeingbackend.persistence.impl.CoachAvailabilityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SlotServiceImplTest {

    @Mock CoachAvailabilityRepository availabilityRepository;
    @Mock AppointmentRepository appointmentRepository;

    @InjectMocks SlotServiceImpl service;

    private UUID coachId;
    private ZoneId coachZone;

    @BeforeEach
    void setup() {
        coachId = UUID.randomUUID();
        coachZone = ZoneId.of("Europe/London");
    }

    // -------------------------------------------------------------------------
    // getSlotsForCoach - validations / empty cases
    // -------------------------------------------------------------------------

    @Test
    void getSlotsForCoach_fromAfterTo_throwsIllegalArgument() {
        LocalDate from = LocalDate.of(2026, 1, 10);
        LocalDate to = LocalDate.of(2026, 1, 9);

        assertThrows(IllegalArgumentException.class, () ->
                service.getSlotsForCoach(coachId, from, to, 60, coachZone)
        );

        verifyNoInteractions(availabilityRepository, appointmentRepository);
    }

    @Test
    void getSlotsForCoach_noAvailabilities_returnsEmpty_andDoesNotQueryAppointments() {
        LocalDate from = LocalDate.of(2026, 1, 10);
        LocalDate to = LocalDate.of(2026, 1, 10);

        when(availabilityRepository.findByCoachIdAndDateBetweenAndIsActiveTrue(coachId, from, to))
                .thenReturn(List.of());

        List<SlotView> out = service.getSlotsForCoach(coachId, from, to, 60, coachZone);

        assertNotNull(out);
        assertTrue(out.isEmpty());

        verify(availabilityRepository).findByCoachIdAndDateBetweenAndIsActiveTrue(coachId, from, to);
        verifyNoInteractions(appointmentRepository);
    }

    // -------------------------------------------------------------------------
    // getSlotsForCoach - repo range arguments
    // -------------------------------------------------------------------------

    @Test
    void getSlotsForCoach_queriesAppointmentsBetween_fromStartOfDay_toNextDayStart() {
        LocalDate from = LocalDate.of(2026, 1, 10);
        LocalDate to = LocalDate.of(2026, 1, 11);

        CoachAvailability a = CoachAvailability.builder()
                .id(UUID.randomUUID())
                .coachId(coachId)
                .date(LocalDate.of(2026, 1, 10))
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(10, 0))
                .isActive(true)
                .build();

        when(availabilityRepository.findByCoachIdAndDateBetweenAndIsActiveTrue(coachId, from, to))
                .thenReturn(List.of(a));
        when(appointmentRepository.findByCoachIdAndStartsAtBetween(eq(coachId), any(), any()))
                .thenReturn(List.of());

        service.getSlotsForCoach(coachId, from, to, 30, coachZone);

        ArgumentCaptor<OffsetDateTime> fromCaptor = ArgumentCaptor.forClass(OffsetDateTime.class);
        ArgumentCaptor<OffsetDateTime> toCaptor = ArgumentCaptor.forClass(OffsetDateTime.class);

        verify(appointmentRepository).findByCoachIdAndStartsAtBetween(eq(coachId), fromCaptor.capture(), toCaptor.capture());

        OffsetDateTime fromDt = fromCaptor.getValue();
        OffsetDateTime toDt = toCaptor.getValue();

        // from = fromDate at 00:00 in coachZone
        OffsetDateTime expectedFrom = from.atStartOfDay(coachZone).toOffsetDateTime();
        // to = (toDate + 1) at 00:00 in coachZone
        OffsetDateTime expectedTo = to.plusDays(1).atStartOfDay(coachZone).toOffsetDateTime();

        assertEquals(expectedFrom.toInstant(), fromDt.toInstant());
        assertEquals(expectedTo.toInstant(), toDt.toInstant());
    }

    // -------------------------------------------------------------------------
    // Slot generation - no bookings
    // -------------------------------------------------------------------------

    @Test
    void getSlotsForCoach_singleAvailability_generatesNonOverlappingSlots_sorted() {
        LocalDate day = LocalDate.of(2026, 1, 10);

        CoachAvailability a = CoachAvailability.builder()
                .id(UUID.randomUUID())
                .coachId(coachId)
                .date(day)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(11, 0))
                .isActive(true)
                .build();

        when(availabilityRepository.findByCoachIdAndDateBetweenAndIsActiveTrue(coachId, day, day))
                .thenReturn(List.of(a));
        when(appointmentRepository.findByCoachIdAndStartsAtBetween(eq(coachId), any(), any()))
                .thenReturn(List.of());

        List<SlotView> out = service.getSlotsForCoach(coachId, day, day, 60, coachZone);

        // 9-10, 10-11 -> 2 slot
        assertEquals(2, out.size());

        assertEquals(day, out.get(0).getDate());
        assertEquals(LocalTime.of(9, 0), out.get(0).getStartTime());
        assertEquals(LocalTime.of(10, 0), out.get(0).getEndTime());
        assertNotNull(out.get(0).getStartsAt());
        assertNotNull(out.get(0).getEndsAt());
        assertEquals(Duration.ofMinutes(60), Duration.between(out.get(0).getStartsAt(), out.get(0).getEndsAt()));

        assertEquals(day, out.get(1).getDate());
        assertEquals(LocalTime.of(10, 0), out.get(1).getStartTime());
        assertEquals(LocalTime.of(11, 0), out.get(1).getEndTime());
    }

    @Test
    void getSlotsForCoach_slotLengthDoesNotFit_returnsEmpty() {
        LocalDate day = LocalDate.of(2026, 1, 10);

        CoachAvailability a = CoachAvailability.builder()
                .id(UUID.randomUUID())
                .coachId(coachId)
                .date(day)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(9, 30))
                .isActive(true)
                .build();

        when(availabilityRepository.findByCoachIdAndDateBetweenAndIsActiveTrue(coachId, day, day))
                .thenReturn(List.of(a));
        when(appointmentRepository.findByCoachIdAndStartsAtBetween(eq(coachId), any(), any()))
                .thenReturn(List.of());

        List<SlotView> out = service.getSlotsForCoach(coachId, day, day, 60, coachZone);

        assertTrue(out.isEmpty());
    }

    // -------------------------------------------------------------------------
    // Slot generation - with bookings / overlap removal
    // -------------------------------------------------------------------------

    @Test
    void getSlotsForCoach_filtersOutOverlappingSlots() {
        LocalDate day = LocalDate.of(2026, 1, 10);

        CoachAvailability a = CoachAvailability.builder()
                .id(UUID.randomUUID())
                .coachId(coachId)
                .date(day)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(12, 0))
                .isActive(true)
                .build();

        // booking 10:00-11:00 (az offsetet nem erőltetjük; a lényeg a overlaps logika)
        ZoneOffset off = coachZone.getRules().getOffset(Instant.now());
        AppointmentEntity booking = AppointmentEntity.builder()
                .id(UUID.randomUUID())
                .coachId(coachId)
                .clientId(UUID.randomUUID())
                .startsAt(OffsetDateTime.of(day, LocalTime.of(10, 0), off))
                .endsAt(OffsetDateTime.of(day, LocalTime.of(11, 0), off))
                .build();

        when(availabilityRepository.findByCoachIdAndDateBetweenAndIsActiveTrue(coachId, day, day))
                .thenReturn(List.of(a));
        when(appointmentRepository.findByCoachIdAndStartsAtBetween(eq(coachId), any(), any()))
                .thenReturn(List.of(booking));

        List<SlotView> out = service.getSlotsForCoach(coachId, day, day, 60, coachZone);

        // availability 9-12, slot=60 => [9-10], [10-11], [11-12]
        // booking 10-11 kizárja a középsőt => 2 marad
        assertEquals(2, out.size());

        assertEquals(LocalTime.of(9, 0), out.get(0).getStartTime());
        assertEquals(LocalTime.of(10, 0), out.get(0).getEndTime());

        assertEquals(LocalTime.of(11, 0), out.get(1).getStartTime());
        assertEquals(LocalTime.of(12, 0), out.get(1).getEndTime());
    }

    @Test
    void getSlotsForCoach_overlapWithPartialIntersection_filtersSlot() {
        LocalDate day = LocalDate.of(2026, 1, 10);

        CoachAvailability a = CoachAvailability.builder()
                .id(UUID.randomUUID())
                .coachId(coachId)
                .date(day)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(11, 0))
                .isActive(true)
                .build();

        // booking 9:30-10:00 => overlaps a 9:00-10:00 slotot
        ZoneOffset off = coachZone.getRules().getOffset(Instant.now());
        AppointmentEntity booking = AppointmentEntity.builder()
                .id(UUID.randomUUID())
                .coachId(coachId)
                .clientId(UUID.randomUUID())
                .startsAt(OffsetDateTime.of(day, LocalTime.of(9, 30), off))
                .endsAt(OffsetDateTime.of(day, LocalTime.of(10, 0), off))
                .build();

        when(availabilityRepository.findByCoachIdAndDateBetweenAndIsActiveTrue(coachId, day, day))
                .thenReturn(List.of(a));
        when(appointmentRepository.findByCoachIdAndStartsAtBetween(eq(coachId), any(), any()))
                .thenReturn(List.of(booking));

        List<SlotView> out = service.getSlotsForCoach(coachId, day, day, 60, coachZone);

        // availability 9-11, slot=60 => [9-10], [10-11]
        // booking 9:30-10:00 kizárja [9-10]-et, [10-11] marad
        assertEquals(1, out.size());
        assertEquals(LocalTime.of(10, 0), out.get(0).getStartTime());
        assertEquals(LocalTime.of(11, 0), out.get(0).getEndTime());
    }

    // -------------------------------------------------------------------------
    // Sorting - multiple availabilities / unsorted input
    // -------------------------------------------------------------------------

    @Test
    void getSlotsForCoach_sortsSlotsByDateThenStartTime() {
        LocalDate d1 = LocalDate.of(2026, 1, 10);
        LocalDate d2 = LocalDate.of(2026, 1, 11);

        CoachAvailability a2 = CoachAvailability.builder()
                .id(UUID.randomUUID())
                .coachId(coachId)
                .date(d2)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(10, 0))
                .isActive(true)
                .build();

        CoachAvailability a1 = CoachAvailability.builder()
                .id(UUID.randomUUID())
                .coachId(coachId)
                .date(d1)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(11, 0))
                .isActive(true)
                .build();

        // repo visszaadhat "véletlen" sorrendben, a service-nek rendeznie kell
        when(availabilityRepository.findByCoachIdAndDateBetweenAndIsActiveTrue(coachId, d1, d2))
                .thenReturn(List.of(a2, a1));
        when(appointmentRepository.findByCoachIdAndStartsAtBetween(eq(coachId), any(), any()))
                .thenReturn(List.of());

        List<SlotView> out = service.getSlotsForCoach(coachId, d1, d2, 60, coachZone);

        assertEquals(2, out.size());
        // elvárt sorrend: d1 10:00, aztán d2 9:00
        assertEquals(d1, out.get(0).getDate());
        assertEquals(LocalTime.of(10, 0), out.get(0).getStartTime());

        assertEquals(d2, out.get(1).getDate());
        assertEquals(LocalTime.of(9, 0), out.get(1).getStartTime());
    }
}
