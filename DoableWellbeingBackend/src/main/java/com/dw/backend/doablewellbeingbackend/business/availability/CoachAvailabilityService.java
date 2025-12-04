package com.dw.backend.doablewellbeingbackend.business.availability;

import com.dw.backend.doablewellbeingbackend.domain.appointment.CoachAvailabilityRequest;
import com.dw.backend.doablewellbeingbackend.domain.appointment.CoachAvailabilityResponse;
import com.dw.backend.doablewellbeingbackend.domain.appointment.CoachAvailabilityView;
import com.dw.backend.doablewellbeingbackend.domain.appointment.SlotView;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

public interface CoachAvailabilityService {
    List<CoachAvailabilityView> createAvailability(UUID coachId, CoachAvailabilityRequest request);

    List<CoachAvailabilityResponse> getAvailabilitiesForCoach( UUID coachId, LocalDate fromDate, LocalDate toDate);

    void deleteAvailability(UUID coachId, UUID availabilityId);

    CoachAvailabilityView setDayOff(UUID coachId, UUID availabilityId);


}
