package com.dw.backend.doablewellbeingbackend.business.availability;

import com.dw.backend.doablewellbeingbackend.domain.appointment.CoachAvailabilityRequest;
import com.dw.backend.doablewellbeingbackend.domain.appointment.CoachAvailabilityResponse;
import com.dw.backend.doablewellbeingbackend.domain.appointment.CoachAvailabilityView;
import com.dw.backend.doablewellbeingbackend.domain.appointment.SlotView;
import com.dw.backend.doablewellbeingbackend.persistence.entity.AppointmentEntity;
import com.dw.backend.doablewellbeingbackend.persistence.entity.CoachAvailability;
import com.dw.backend.doablewellbeingbackend.persistence.impl.AppointmentRepository;
import com.dw.backend.doablewellbeingbackend.persistence.impl.CoachAvailabilityRepository;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CoachAvailabilityServiceImpl implements CoachAvailabilityService {

    private final CoachAvailabilityRepository availabilityRepository;
    private final AppointmentRepository appointmentRepository;

    @Override
    @Transactional
    public List<CoachAvailabilityView> createAvailability(UUID coachId, CoachAvailabilityRequest request) {
        validateTimes(request);

        int repeatWeeks = Optional.ofNullable(request.getRepeatWeeks()).orElse(1);
        boolean recurring = request.isRecurring() && repeatWeeks > 1;

        UUID seriesId = recurring ? UUID.randomUUID() : null;

        List<CoachAvailability> entities = new ArrayList<>();

        for (int i = 0; i < repeatWeeks; i++) {
            LocalDate date = request.getDate().plusWeeks(i);

            CoachAvailability entity = CoachAvailability.builder()
                    .coachId(coachId)
                    .date(date)
                    // ha az entitásban még van weekday mező, töltsük is ki:
                    // .weekday(date.getDayOfWeek().getValue())
                    .startTime(request.getStartTime())
                    .endTime(request.getEndTime())
                    .isRecurring(recurring)
                    .seriesId(seriesId)
                    .isActive(true)
                    .build();

            entities.add(entity);
        }

        List<CoachAvailability> saved = availabilityRepository.saveAll(entities);

        return saved.stream()
                .map(this::toView)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CoachAvailabilityView setDayOff(UUID coachId, UUID availabilityId) {



        CoachAvailability availability = availabilityRepository.findById(availabilityId)
                .orElseThrow(() -> new NoSuchElementException("Availability not found"));


        if (!availability.getCoachId().equals(coachId)) {
            throw new SecurityException("Cannot delete another coach's availability");
        }

        availability.setActive(false);
        availabilityRepository.save(availability);
        return toView(availability);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CoachAvailabilityResponse> getAvailabilitiesForCoach(
            UUID coachId,
            LocalDate fromDate,
            LocalDate toDate
    ) {
        if (fromDate.isAfter(toDate)) {
            throw new IllegalArgumentException("fromDate must be <= toDate");
        }

        return availabilityRepository
                .findByCoachIdAndDateBetweenOrderByDateAscStartTimeAsc(coachId, fromDate, toDate)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }



    @Override
    @Transactional
    public void deleteAvailability(UUID coachId, UUID availabilityId) {
        CoachAvailability availability = availabilityRepository.findById(availabilityId)
                .orElseThrow(() -> new IllegalArgumentException("Availability not found"));

        if (!availability.getCoachId().equals(coachId)) {
            throw new SecurityException("Cannot delete another coach's availability");
        }

        availabilityRepository.delete(availability);
    }



    private void validateTimes(CoachAvailabilityRequest request) {
        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new IllegalArgumentException("endTime must be after startTime");
        }
    }

    private CoachAvailabilityView toView(CoachAvailability entity) {
        return CoachAvailabilityView.builder()
                .id(entity.getId())
                .date(entity.getDate())
                .startTime(entity.getStartTime())
                .endTime(entity.getEndTime())
                .recurring(entity.isRecurring())
                .seriesId(entity.getSeriesId())
                .active(entity.isActive())
                .build();
    }

    private CoachAvailabilityResponse mapToResponse(CoachAvailability availability) {

        return CoachAvailabilityResponse.builder()
                .id(availability.getId())
                .date(availability.getDate())
                .startTime(availability.getStartTime())
                .endTime(availability.getEndTime())
                .recurring(availability.isRecurring())
                .seriesId(availability.getSeriesId())
                .active(availability.isActive())
                .build();
    }



}
