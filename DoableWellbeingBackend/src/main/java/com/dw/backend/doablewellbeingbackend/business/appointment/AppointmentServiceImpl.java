package com.dw.backend.doablewellbeingbackend.business.appointment;

import com.dw.backend.doablewellbeingbackend.business.google.GoogleMeetService;
import com.dw.backend.doablewellbeingbackend.common.exception.AccessDeniedException;
import com.dw.backend.doablewellbeingbackend.common.exception.NotFoundException;
import com.dw.backend.doablewellbeingbackend.domain.enums.AppointmentStatus;
import com.dw.backend.doablewellbeingbackend.persistence.entity.AppointmentEntity;

import com.dw.backend.doablewellbeingbackend.persistence.impl.AppointmentRepository;
import com.dw.backend.doablewellbeingbackend.persistence.impl.CoachRepository;
import com.dw.backend.doablewellbeingbackend.persistence.impl.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {

    private static final Set<Integer> ALLOWED_SLOT_DURATIONS = Set.of(120, 60, 45, 30, 15);
    private static final List<AppointmentStatus> COUNT_AS_EXISTING = List.of(AppointmentStatus.SCHEDULED, AppointmentStatus.COMPLETED);

    private final AppointmentRepository apptRepo;
    private final CoachRepository coachRepo;
    private final UserRepository userRepo;
    private final GoogleMeetService meetService;


    @Override
    @Transactional
    public AppointmentEntity requestAppointmentFromSlot(UUID coachId, UUID clientId, OffsetDateTime slotStart, int durationMinutes, String notes) {
        if (!ALLOWED_SLOT_DURATIONS.contains(durationMinutes)) {
            throw new IllegalArgumentException(
                    "Invalid slot duration. Allowed values: 120, 60, 45, 30, 15 minutes."
            );
        }

        OffsetDateTime slotEnd = slotStart.plusMinutes(durationMinutes);

        return createAppointmentWithIntroLogic(
                coachId,
                clientId,
                slotStart,
                slotEnd,
                notes
        );
    }

    @Override
    @Transactional
    public AppointmentEntity confirmAppointment(UUID coachId, UUID appointmentId) {
        AppointmentEntity appt = apptRepo.findById(appointmentId)
                .orElseThrow(() -> new NotFoundException("Appointment not found"));

        if (!appt.getCoachId().equals(coachId)) {
            throw new AccessDeniedException("You can only confirm your own appointments");
        }

        if (appt.getStatus() != AppointmentStatus.REQUESTED) {
            throw new IllegalStateException("Only requested appointments can be confirmed");
        }

        try {
            meetService.attachGoogleMeetToAppointment(appt);
        } catch (IOException ex) {
            throw new RuntimeException("Failed to create Google Meet link", ex);
        }

        appt.setStatus(AppointmentStatus.SCHEDULED);
        appt.setConfirmedAt(OffsetDateTime.now());

        return apptRepo.save(appt);
    }

    @Override
    @Transactional
    public AppointmentEntity declineAppointment(UUID coachId, UUID appointmentId, String reason) {
        AppointmentEntity appt = apptRepo.findById(appointmentId)
                .orElseThrow(() -> new NotFoundException("Appointment not found"));

        if (!appt.getCoachId().equals(coachId)) {
            throw new AccessDeniedException("You can only decline your own appointments");
        }

        if (appt.getStatus() != AppointmentStatus.REQUESTED) {
            throw new IllegalStateException("Only requested appointments can be declined");
        }

        appt.setStatus(AppointmentStatus.DECLINED);

        return apptRepo.save(appt);
    }

    private AppointmentEntity createAppointmentWithIntroLogic(UUID coachId, UUID clientId, OffsetDateTime startsAt, OffsetDateTime endsAt, String notes) {
        var coach = coachRepo.findById(coachId)
                .orElseThrow(() -> new NotFoundException("Coach not found"));

        var client = userRepo.findById(clientId)
                .orElseThrow(() -> new NotFoundException("Client not found"));

        if (!endsAt.isAfter(startsAt)) {
            throw new IllegalArgumentException("End time must be after start time");
        }

        boolean overlaps = apptRepo.existsOverlap(coachId, startsAt, endsAt);
        if (overlaps) {
            throw new IllegalStateException("Appointment overlaps with existing booking");
        }


        boolean hasExistingSession = apptRepo.existsByCoachIdAndClientIdAndStatusIn(
                coachId,
                clientId,
                COUNT_AS_EXISTING
        );

        AppointmentEntity appt = AppointmentEntity.builder()
                .coachId(coach.getUserId())
                .clientId(client.getId())
                .startsAt(startsAt)
                .endsAt(endsAt)
                .notes(notes)
                .build();

        if (!hasExistingSession) {

            appt.setStatus(AppointmentStatus.REQUESTED);
        } else {

            appt.setStatus(AppointmentStatus.SCHEDULED);
            appt.setConfirmedAt(OffsetDateTime.now());

            try {
                meetService.attachGoogleMeetToAppointment(appt);
            } catch (IOException ex) {

                throw new RuntimeException("Failed to create Google Meet link", ex);
            }
        }

        return apptRepo.save(appt);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentEntity> getAppointmentsForClient(UUID clientId) {
        return apptRepo.findByClientIdOrderByStartsAtDesc((clientId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentEntity> getAppointmentsForCoach(UUID coachId) {
        return apptRepo.findByCoachIdOrderByStartsAtDesc((coachId));
    }


    @Override
    @Transactional
    public AppointmentEntity cancelAppointmentAsClient(UUID clientId, UUID appointmentId) {
        AppointmentEntity appt = apptRepo.findById(appointmentId)
                .orElseThrow(() -> new NotFoundException("Appointment not found"));

        if (!appt.getClientId().equals(clientId)) {
            throw new AccessDeniedException("Not your appointment");
        }

        if (appt.getStatus() != AppointmentStatus.REQUESTED &&
                appt.getStatus() != AppointmentStatus.SCHEDULED) {
            throw new IllegalStateException("Only pending or scheduled appointments can be cancelled");
        }

        if (appt.getStartsAt().isBefore(OffsetDateTime.now())) {
            throw new IllegalStateException("Cannot cancel past appointments");
        }

        appt.setStatus(AppointmentStatus.CANCELLED);

        return apptRepo.save(appt);
    }

    @Override
    @Transactional
    public AppointmentEntity cancelAppointmentAsCoach(UUID coachId, UUID appointmentId) {
        AppointmentEntity appt = apptRepo.findById(appointmentId)
                .orElseThrow(() -> new NotFoundException("Appointment not found"));

        if (!appt.getCoachId().equals(coachId)) {
            throw new AccessDeniedException("Not your appointment");
        }

        if (appt.getStatus() != AppointmentStatus.REQUESTED &&
                appt.getStatus() != AppointmentStatus.SCHEDULED) {
            throw new IllegalStateException("Only pending or scheduled appointments can be cancelled");
        }

        if (appt.getStartsAt().isBefore(OffsetDateTime.now())) {
            throw new IllegalStateException("Cannot cancel past appointments");
        }

        appt.setStatus(AppointmentStatus.CANCELLED);

        return apptRepo.save(appt);
    }
}
