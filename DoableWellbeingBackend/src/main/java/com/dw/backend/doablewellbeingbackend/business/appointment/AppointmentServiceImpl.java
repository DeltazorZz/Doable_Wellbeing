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
import com.dw.backend.doablewellbeingbackend.persistence.entity.UserEntity;
import com.dw.backend.doablewellbeingbackend.persistence.impl.*;
import com.google.api.services.calendar.model.Event;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {

    private static final Set<Integer> ALLOWED_SLOT_DURATIONS =
            Set.of(120, 60, 45, 30, 15);

    private static final List<AppointmentStatus> COUNT_AS_EXISTING =
            List.of(AppointmentStatus.scheduled, AppointmentStatus.completed);

    private final AppointmentRepository apptRepo;
    private final CoachRepository coachRepo;
    private final UserRepository userRepo;
    private final GoogleCalendarService googleCalendarService;
    private final AppointmentNoteRepository appointmentNoteRepository;
    private final AppointmentResourceRepository appointmentResourceRepository;

    // -------------------------------------------------------------------------
    // REQUEST APPOINTMENT
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public AppointmentEntity requestAppointmentFromSlot(
            UUID coachId,
            UUID clientId,
            OffsetDateTime slotStart,
            int durationMinutes,
            String notes
    ) {
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
    public AppointmentEntity completeAppointment(UUID coachId, UUID appointmentId) {
        AppointmentEntity appt = apptRepo.findById(appointmentId)
                .orElseThrow(() -> new NotFoundException("Appointment not found"));

        if (!appt.getCoachId().equals(coachId)) {
            throw new AccessDeniedException("You can only confirm your own appointments");
        }

        if (appt.getStatus() != AppointmentStatus.scheduled) {
            throw new IllegalStateException("Only requested appointments can be confirmed");
        }

        appt.setStatus(AppointmentStatus.completed);

        return apptRepo.save(appt);
    }

    // -------------------------------------------------------------------------
    // CONFIRM APPOINTMENT
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public AppointmentEntity confirmAppointment(UUID coachId, UUID appointmentId) {
        AppointmentEntity appt = apptRepo.findById(appointmentId)
                .orElseThrow(() -> new NotFoundException("Appointment not found"));

        if (!appt.getCoachId().equals(coachId)) {
            throw new AccessDeniedException("You can only confirm your own appointments");
        }

        if (appt.getStatus() != AppointmentStatus.requested) {
            throw new IllegalStateException("Only requested appointments can be confirmed");
        }

        try {
            attachGoogleMeetToAppointment(appt);
        } catch (IOException ex) {
            throw new RuntimeException("Failed to create Google Meet event", ex);
        }

        appt.setStatus(AppointmentStatus.scheduled);
        appt.setConfirmedAt(OffsetDateTime.now());

        return apptRepo.save(appt);
    }

    // -------------------------------------------------------------------------
    // DECLINE
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public AppointmentEntity declineAppointment(UUID coachId, UUID appointmentId, String reason) {
        AppointmentEntity appt = apptRepo.findById(appointmentId)
                .orElseThrow(() -> new NotFoundException("Appointment not found"));

        if (!appt.getCoachId().equals(coachId)) {
            throw new AccessDeniedException("You can only decline your own appointments");
        }

        if (appt.getStatus() != AppointmentStatus.requested) {
            throw new IllegalStateException("Only requested appointments can be declined");
        }

        appt.setStatus(AppointmentStatus.declined);
        return apptRepo.save(appt);
    }

    // -------------------------------------------------------------------------
    // GETTERS
    // -------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentEntity> getAppointmentsForClient(UUID clientId) {
        return apptRepo.findByClientIdOrderByStartsAtDesc(clientId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentEntity> getAppointmentsForCoach(UUID coachId) {
        return apptRepo.findByCoachIdOrderByStartsAtDesc(coachId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentEntity> getAppointmentsForCoachRange(UUID coachId, OffsetDateTime from, OffsetDateTime to) {
        return apptRepo.findByCoachIdAndStartsAtBetweenOrderByStartsAtAsc(coachId, from, to);
    }


    // -------------------------------------------------------------------------
    // CANCEL
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public AppointmentEntity cancelAppointmentAsClient(UUID clientId, UUID appointmentId) {
        AppointmentEntity appt = apptRepo.findById(appointmentId)
                .orElseThrow(() -> new NotFoundException("Appointment not found"));

        if (!appt.getClientId().equals(clientId)) {
            throw new AccessDeniedException("Not your appointment");
        }

        if (!canCancel(appt)) {
            throw new IllegalStateException("Cannot cancel this appointment");
        }

        appt.setStatus(AppointmentStatus.cancelled);
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

        if (!canCancel(appt)) {
            throw new IllegalStateException("Cannot cancel this appointment");
        }

        appt.setStatus(AppointmentStatus.cancelled);
        return apptRepo.save(appt);
    }

    private boolean canCancel(AppointmentEntity appt) {
        return (appt.getStatus() == AppointmentStatus.requested ||
                appt.getStatus() == AppointmentStatus.scheduled)
                && appt.getStartsAt().isAfter(OffsetDateTime.now());
    }

    // -------------------------------------------------------------------------
    // INSTANT BOOKING
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public AppointmentEntity instantBookFromSlot(
            UUID coachId,
            UUID clientId,
            OffsetDateTime slotStart,
            int durationMinutes,
            String notes
    ) {
        if (!ALLOWED_SLOT_DURATIONS.contains(durationMinutes)) {
            throw new IllegalArgumentException(
                    "Invalid slot duration."
            );
        }

        var coach = coachRepo.findById(coachId)
                .orElseThrow(() -> new NotFoundException("Coach not found"));

        var client = userRepo.findById(clientId)
                .orElseThrow(() -> new NotFoundException("Client not found"));

        OffsetDateTime slotEnd = slotStart.plusMinutes(durationMinutes);

        if (!slotEnd.isAfter(slotStart)) {
            throw new IllegalArgumentException("End time must be after start time");
        }

        boolean overlaps = apptRepo.existsOverlap(coachId, slotStart, slotEnd);
        if (overlaps) throw new IllegalStateException("Appointment overlaps");

        AppointmentEntity appt = AppointmentEntity.builder()
                .coachId(coach.getUserId())
                .clientId(client.getId())
                .startsAt(slotStart)
                .endsAt(slotEnd)
                .notes(notes)
                .status(AppointmentStatus.scheduled)
                .confirmedAt(OffsetDateTime.now())
                .build();

        try {
            attachGoogleMeetToAppointment(appt);
        } catch (IOException ex) {
            throw new RuntimeException("Failed to create event", ex);
        }

        return apptRepo.save(appt);
    }

    // -------------------------------------------------------------------------
    // GOOGLE MEET + CALENDAR LOGIC
    // -------------------------------------------------------------------------

    private void attachGoogleMeetToAppointment(AppointmentEntity appt) throws IOException {

        if (!googleCalendarService.isEnabled) {
            appt.setExternalCalendarProvider("google");
            appt.setMeetingUrl("https://meet.google.com/test-meeting");
            appt.setExternalCalendarId("test");
            return;
        }


        UserEntity client = userRepo.findById(appt.getClientId())
                .orElseThrow(() -> new NotFoundException("Client not found"));

        ZonedDateTime startZdt = appt.getStartsAt().atZoneSameInstant(ZoneId.systemDefault());
        ZonedDateTime endZdt   = appt.getEndsAt().atZoneSameInstant(ZoneId.systemDefault());

        String fullName = buildClientDisplayName(client);

        Event event = googleCalendarService.createEventWithMeet(
                "Coaching session with " + fullName,
                appt.getNotes() != null ? appt.getNotes() : "Doable Wellbeing coaching session",
                client.getEmail(),
                startZdt,
                endZdt
        );


        appt.setExternalCalendarId(event.getId());
        appt.setExternalCalendarProvider("google");
        appt.setMeetingUrl(event.getHangoutLink());
    }

    private String buildClientDisplayName(UserEntity client) {
        String first = client.getFirstName() != null ? client.getFirstName() : "";
        String last  = client.getLastName() != null ? client.getLastName() : "";

        String full = (first + " " + last).trim();

        return full.isEmpty() ? client.getEmail() : full;
    }

    // -------------------------------------------------------------------------
    // INTRO LOGIC FOR FIRST SESSION
    // -------------------------------------------------------------------------

    private AppointmentEntity createAppointmentWithIntroLogic(
            UUID coachId,
            UUID clientId,
            OffsetDateTime startsAt,
            OffsetDateTime endsAt,
            String notes
    ) {
        var coach = coachRepo.findById(coachId)
                .orElseThrow(() -> new NotFoundException("Coach not found"));

        var client = userRepo.findById(clientId)
                .orElseThrow(() -> new NotFoundException("Client not found"));

        if (!endsAt.isAfter(startsAt)) {
            throw new IllegalArgumentException("End time must be after start time");
        }

        boolean overlaps = apptRepo.existsOverlap(coachId, startsAt, endsAt);
        if (overlaps) throw new IllegalStateException("Appointment overlaps");

        boolean hasExisting = apptRepo.existsByCoachIdAndClientIdAndStatusIn(
                coachId, clientId, COUNT_AS_EXISTING
        );

        AppointmentEntity appt = AppointmentEntity.builder()
                .coachId(coach.getUserId())
                .clientId(client.getId())
                .startsAt(startsAt)
                .endsAt(endsAt)
                .notes(notes)
                .build();

        if (!hasExisting) {

            appt.setStatus(AppointmentStatus.requested);
        } else {
            appt.setStatus(AppointmentStatus.scheduled);
            appt.setConfirmedAt(OffsetDateTime.now());

            try {
                attachGoogleMeetToAppointment(appt);
            } catch (IOException ex) {
                throw new RuntimeException("Failed to create event", ex);
            }
        }

        return apptRepo.save(appt);
    }


    @Override
    @Transactional(readOnly = true)
    public List<CoachCalendarEventView> getCoachCalendar(
            UUID coachId,
            OffsetDateTime from,
            OffsetDateTime to
    ) {
        return apptRepo.findCoachCalendar(coachId, from, to)
                .stream()
                .map(p -> new CoachCalendarEventView(
                        p.getId(),
                        p.getClientName(),
                        p.getStartsAt(),
                        p.getEndsAt(),
                        p.getStatus(),
                        p.getMeetingUrl(),
                        p.getExternalCalendarId(),
                        new ClientSummaryView(
                                p.getClientId(),
                                p.getClientName(),
                                p.getClientEmail()
                        ),
                        previewNotes(p.getNotes())
                ))
                .toList();
    }

    private String previewNotes(String notes) {
        if (notes == null || notes.isBlank()) return null;
        return notes.length() <= 120
                ? notes
                : notes.substring(0, 120) + "…";
    }


    @Transactional
    public void addNote(UUID coachId, UUID appointmentId, AddAppointmentNoteRequest req) {
        var appt = apptRepo.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found"));

        if (!appt.getCoachId().equals(coachId))
            throw new IllegalArgumentException("Access denied");

        appointmentNoteRepository.save(AppointmentNoteEntity.builder()
                .appointmentId(appointmentId)
                .createdBy(coachId)
                .note(req.note())
                .createdAt(OffsetDateTime.now())
                .build());
    }

    @Transactional
    public UUID addResource(UUID coachId, UUID appointmentId, AddAppointmentResourceRequest req) {
        var appt = apptRepo.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found"));

        if (!appt.getCoachId().equals(coachId))
            throw new IllegalArgumentException("Access denied");

        var saved = appointmentResourceRepository.save(AppointmentResourceEntity.builder()
                .appointmentId(appointmentId)
                .uploadedBy(coachId)
                .fileName(req.fileName())
                .sizeBytes(req.sizeBytes())
                .mimeType(req.mimeType())
                .url(req.url())
                .createdAt(OffsetDateTime.now())
                .build());

        return saved.getId();
    }

}
