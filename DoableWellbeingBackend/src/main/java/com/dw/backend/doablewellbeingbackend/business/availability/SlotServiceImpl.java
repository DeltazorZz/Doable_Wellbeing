package com.dw.backend.doablewellbeingbackend.business.availability;

import com.dw.backend.doablewellbeingbackend.domain.appointment.SlotView;
import com.dw.backend.doablewellbeingbackend.domain.appointment.TimeSlotResponse;
import com.dw.backend.doablewellbeingbackend.persistence.entity.AppointmentEntity;
import com.dw.backend.doablewellbeingbackend.persistence.entity.CoachAvailability;
import com.dw.backend.doablewellbeingbackend.persistence.impl.AppointmentRepository;
import com.dw.backend.doablewellbeingbackend.persistence.impl.CoachAvailabilityRepository;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SlotServiceImpl implements SlotService {
    private static final Set<Integer> ALLOWED_DURATIONS = Set.of(120, 60, 45, 30, 15);

    private final CoachAvailabilityRepository availabilityRepository;
    private final AppointmentRepository appointmentRepository;


    private static final ZoneId zoneId = ZoneId.of("Europe/London");

    @Override
    @Transactional(readOnly = true)
    public List<SlotView> getSlotsForCoach(
            UUID coachId,
            LocalDate fromDate,
            LocalDate toDate,
            int slotLengthMinutes,
            ZoneId coachZone
    ) {
        if (fromDate.isAfter(toDate)) {
            throw new IllegalArgumentException("fromDate must be <= toDate");
        }

        List<CoachAvailability> availabilities =
                availabilityRepository.findByCoachIdAndDateBetweenAndIsActiveTrue(coachId, fromDate, toDate);

        if (availabilities.isEmpty()) {
            return List.of();
        }


        OffsetDateTime fromDateTime = fromDate.atStartOfDay(coachZone).toOffsetDateTime();
        OffsetDateTime toDateTime = toDate.plusDays(1).atStartOfDay(coachZone).toOffsetDateTime();

        List<AppointmentEntity> appointments = appointmentRepository
                .findByCoachIdAndStartsAtBetween(coachId, fromDateTime, toDateTime);

        return generateSlots(availabilities, appointments, slotLengthMinutes, coachZone);
    }


    @Value
    private static class TimeInterval {
        OffsetDateTime start;
        OffsetDateTime end;

        boolean overlaps(TimeInterval other) {
            return start.isBefore(other.getEnd()) && other.getStart().isBefore(end);
        }
    }

    private List<SlotView> generateSlots(
            List<CoachAvailability> availabilities,
            List<AppointmentEntity> appointments,
            int slotLengthMinutes,
            ZoneId coachZone
    ) {
        List<TimeInterval> bookedIntervals = appointments.stream()
                .map(a -> new TimeInterval(a.getStartsAt(), a.getEndsAt()))
                .toList();

        List<SlotView> slots = new ArrayList<>();
        Duration slotLen = Duration.ofMinutes(slotLengthMinutes);

        for (CoachAvailability availability : availabilities) {
            LocalDate date = availability.getDate();
            LocalTime start = availability.getStartTime();
            LocalTime end = availability.getEndTime();

            LocalTime cursor = start;

            while (!cursor.plus(slotLen).isAfter(end)) {
                LocalTime slotEndTime = cursor.plus(slotLen);


                ZoneOffset offset = coachZone.getRules().getOffset(Instant.now());

                OffsetDateTime slotStart = OffsetDateTime.of(date, cursor, offset);
                OffsetDateTime slotEnd = OffsetDateTime.of(date, slotEndTime, offset);

                TimeInterval slotInterval = new TimeInterval(slotStart, slotEnd);

                boolean overlaps = bookedIntervals.stream()
                        .anyMatch(b -> b.overlaps(slotInterval));

                if (!overlaps) {
                    slots.add(SlotView.builder()
                            .date(date)
                            .startTime(cursor)
                            .endTime(slotEndTime)
                            .startsAt(slotStart)
                            .endsAt(slotEnd)
                            .build());
                }

                cursor = cursor.plus(slotLen);
            }
        }

        slots.sort(Comparator
                .comparing(SlotView::getDate)
                .thenComparing(SlotView::getStartTime));

        return slots;
    }


}
