package com.dw.backend.doablewellbeingbackend.business.availability;

import com.dw.backend.doablewellbeingbackend.domain.appointment.CoachAvailabilityRequest;
import com.dw.backend.doablewellbeingbackend.domain.appointment.CoachAvailabilityResponse;
import com.dw.backend.doablewellbeingbackend.domain.appointment.CoachAvailabilityView;
import com.dw.backend.doablewellbeingbackend.persistence.entity.CoachAvailability;
import com.dw.backend.doablewellbeingbackend.persistence.impl.AppointmentRepository;
import com.dw.backend.doablewellbeingbackend.persistence.impl.CoachAvailabilityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CoachAvailabilityServiceImplTest {

    @Mock CoachAvailabilityRepository availabilityRepository;
    @Mock AppointmentRepository appointmentRepository;

    @InjectMocks CoachAvailabilityServiceImpl service;

    private UUID coachId;
    private UUID otherCoachId;

    @BeforeEach
    void setup() {
        coachId = UUID.randomUUID();
        otherCoachId = UUID.randomUUID();
    }

    // -------------------------------------------------------------------------
    // CREATE AVAILABILITY
    // -------------------------------------------------------------------------

    @Test
    void createAvailability_endBeforeOrEqualStart_throwsIllegalArgument() {
        CoachAvailabilityRequest req = mock(CoachAvailabilityRequest.class);
        when(req.getStartTime()).thenReturn(LocalTime.of(10, 0));
        when(req.getEndTime()).thenReturn(LocalTime.of(10, 0)); // invalid

        assertThrows(IllegalArgumentException.class, () ->
                service.createAvailability(coachId, req)
        );

        verifyNoInteractions(availabilityRepository, appointmentRepository);
    }


    @Test
    void createAvailability_repeatWeeksNull_defaultsTo1_nonRecurring() {
        CoachAvailabilityRequest req = mock(CoachAvailabilityRequest.class);
        LocalDate baseDate = LocalDate.of(2026, 1, 10);
        LocalTime start = LocalTime.of(9, 0);
        LocalTime end = LocalTime.of(12, 0);

        when(req.getDate()).thenReturn(baseDate);
        when(req.getStartTime()).thenReturn(start);
        when(req.getEndTime()).thenReturn(end);
        when(req.getRepeatWeeks()).thenReturn(null); // -> default 1
        when(req.isRecurring()).thenReturn(true); // repeatWeeks=1 miatt NEM recurring

        // capture saveAll input
        ArgumentCaptor<List<CoachAvailability>> captor = ArgumentCaptor.forClass(List.class);

        when(availabilityRepository.saveAll(anyList()))
                .thenAnswer(inv -> inv.getArgument(0)); // returns same list, like saved

        List<CoachAvailabilityView> out = service.createAvailability(coachId, req);

        assertEquals(1, out.size());

        verify(availabilityRepository).saveAll(captor.capture());
        List<CoachAvailability> toSave = captor.getValue();
        assertEquals(1, toSave.size());

        CoachAvailability e = toSave.get(0);
        assertEquals(coachId, e.getCoachId());
        assertEquals(baseDate, e.getDate());
        assertEquals(start, e.getStartTime());
        assertEquals(end, e.getEndTime());

        assertFalse(e.isRecurring());
        assertNull(e.getSeriesId());
        assertTrue(e.isActive());
    }

    @Test
    void createAvailability_recurring_repeatWeeks3_createsSeriesId_andThreeWeeks() {
        CoachAvailabilityRequest req = mock(CoachAvailabilityRequest.class);
        LocalDate baseDate = LocalDate.of(2026, 1, 10);
        LocalTime start = LocalTime.of(9, 0);
        LocalTime end = LocalTime.of(12, 0);

        when(req.getDate()).thenReturn(baseDate);
        when(req.getStartTime()).thenReturn(start);
        when(req.getEndTime()).thenReturn(end);
        when(req.getRepeatWeeks()).thenReturn(3);
        when(req.isRecurring()).thenReturn(true);

        ArgumentCaptor<List<CoachAvailability>> captor = ArgumentCaptor.forClass(List.class);

        when(availabilityRepository.saveAll(anyList()))
                .thenAnswer(inv -> inv.getArgument(0));

        List<CoachAvailabilityView> out = service.createAvailability(coachId, req);

        assertEquals(3, out.size());
        verify(availabilityRepository).saveAll(captor.capture());

        List<CoachAvailability> saved = captor.getValue();
        assertEquals(3, saved.size());

        // all have same seriesId, recurring true
        UUID seriesId0 = saved.get(0).getSeriesId();
        assertNotNull(seriesId0);

        for (int i = 0; i < 3; i++) {
            CoachAvailability e = saved.get(i);

            assertEquals(coachId, e.getCoachId());
            assertEquals(baseDate.plusWeeks(i), e.getDate());
            assertEquals(start, e.getStartTime());
            assertEquals(end, e.getEndTime());
            assertTrue(e.isRecurring());
            assertEquals(seriesId0, e.getSeriesId());
            assertTrue(e.isActive());
        }
    }

    // -------------------------------------------------------------------------
    // SET DAY OFF
    // -------------------------------------------------------------------------

    @Test
    void setDayOff_notFound_throwsNoSuchElement() {
        UUID availabilityId = UUID.randomUUID();
        when(availabilityRepository.findById(availabilityId)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () ->
                service.setDayOff(coachId, availabilityId)
        );

        verify(availabilityRepository, never()).save(any());
    }

    @Test
    void setDayOff_wrongCoach_throwsSecurityException() {
        UUID availabilityId = UUID.randomUUID();
        CoachAvailability entity = CoachAvailability.builder()
                .id(availabilityId)
                .coachId(otherCoachId)
                .date(LocalDate.of(2026, 1, 10))
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(10, 0))
                .isActive(true)
                .build();

        when(availabilityRepository.findById(availabilityId)).thenReturn(Optional.of(entity));

        assertThrows(SecurityException.class, () ->
                service.setDayOff(coachId, availabilityId)
        );

        verify(availabilityRepository, never()).save(any());
    }

    @Test
    void setDayOff_success_setsInactive_andSaves_andReturnsView() {
        UUID availabilityId = UUID.randomUUID();
        CoachAvailability entity = CoachAvailability.builder()
                .id(availabilityId)
                .coachId(coachId)
                .date(LocalDate.of(2026, 1, 10))
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(10, 0))
                .isActive(true)
                .build();

        when(availabilityRepository.findById(availabilityId)).thenReturn(Optional.of(entity));
        when(availabilityRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CoachAvailabilityView out = service.setDayOff(coachId, availabilityId);

        assertNotNull(out);
        assertEquals(availabilityId, out.getId());
        assertFalse(out.isActive());
        assertFalse(entity.isActive()); // side effect on entity
        verify(availabilityRepository).save(entity);
    }

    // -------------------------------------------------------------------------
    // GET AVAILABILITIES FOR COACH
    // -------------------------------------------------------------------------

    @Test
    void getAvailabilitiesForCoach_fromAfterTo_throwsIllegalArgument() {
        LocalDate from = LocalDate.of(2026, 1, 20);
        LocalDate to = LocalDate.of(2026, 1, 10);

        assertThrows(IllegalArgumentException.class, () ->
                service.getAvailabilitiesForCoach(coachId, from, to)
        );

        verifyNoInteractions(availabilityRepository);
    }

    @Test
    void getAvailabilitiesForCoach_returnsMappedResponses_sortedByRepoContract() {
        LocalDate from = LocalDate.of(2026, 1, 1);
        LocalDate to = LocalDate.of(2026, 1, 31);

        CoachAvailability a1 = CoachAvailability.builder()
                .id(UUID.randomUUID())
                .coachId(coachId)
                .date(LocalDate.of(2026, 1, 10))
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(10, 0))
                .isRecurring(false)
                .seriesId(null)
                .isActive(true)
                .build();

        CoachAvailability a2 = CoachAvailability.builder()
                .id(UUID.randomUUID())
                .coachId(coachId)
                .date(LocalDate.of(2026, 1, 11))
                .startTime(LocalTime.of(11, 0))
                .endTime(LocalTime.of(12, 0))
                .isRecurring(true)
                .seriesId(UUID.randomUUID())
                .isActive(false)
                .build();

        when(availabilityRepository.findByCoachIdAndDateBetweenOrderByDateAscStartTimeAsc(coachId, from, to))
                .thenReturn(List.of(a1, a2));

        List<CoachAvailabilityResponse> out =
                service.getAvailabilitiesForCoach(coachId, from, to);

        assertEquals(2, out.size());

        CoachAvailabilityResponse r1 = out.get(0);
        assertEquals(a1.getId(), r1.id());
        assertEquals(a1.getDate(), r1.date());
        assertEquals(a1.getStartTime(), r1.startTime());
        assertEquals(a1.getEndTime(), r1.endTime());
        assertEquals(a1.isRecurring(), r1.recurring());
        assertEquals(a1.getSeriesId(), r1.seriesId());
        assertEquals(a1.isActive(), r1.active());

        CoachAvailabilityResponse r2 = out.get(1);
        assertEquals(a2.getId(), r2.id());
        assertEquals(a2.getDate(), r2.date());
        assertEquals(a2.getStartTime(), r2.startTime());
        assertEquals(a2.getEndTime(), r2.endTime());
        assertEquals(a2.isRecurring(), r2.recurring());
        assertEquals(a2.getSeriesId(), r2.seriesId());
        assertEquals(a2.isActive(), r2.active());

        verify(availabilityRepository).findByCoachIdAndDateBetweenOrderByDateAscStartTimeAsc(coachId, from, to);
    }

    // -------------------------------------------------------------------------
    // DELETE AVAILABILITY
    // -------------------------------------------------------------------------

    @Test
    void deleteAvailability_notFound_throwsIllegalArgument() {
        UUID availabilityId = UUID.randomUUID();
        when(availabilityRepository.findById(availabilityId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                service.deleteAvailability(coachId, availabilityId)
        );

        verify(availabilityRepository, never()).delete(any());
    }

    @Test
    void deleteAvailability_wrongCoach_throwsSecurityException() {
        UUID availabilityId = UUID.randomUUID();
        CoachAvailability entity = CoachAvailability.builder()
                .id(availabilityId)
                .coachId(otherCoachId)
                .build();

        when(availabilityRepository.findById(availabilityId)).thenReturn(Optional.of(entity));

        assertThrows(SecurityException.class, () ->
                service.deleteAvailability(coachId, availabilityId)
        );

        verify(availabilityRepository, never()).delete(any());
    }

    @Test
    void deleteAvailability_success_deletesEntity() {
        UUID availabilityId = UUID.randomUUID();
        CoachAvailability entity = CoachAvailability.builder()
                .id(availabilityId)
                .coachId(coachId)
                .build();

        when(availabilityRepository.findById(availabilityId)).thenReturn(Optional.of(entity));

        service.deleteAvailability(coachId, availabilityId);

        verify(availabilityRepository).delete(entity);
    }
}
