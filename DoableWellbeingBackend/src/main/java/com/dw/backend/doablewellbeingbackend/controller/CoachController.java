package com.dw.backend.doablewellbeingbackend.controller;

import com.dw.backend.doablewellbeingbackend.business.appointment.AppointmentMapper;
import com.dw.backend.doablewellbeingbackend.business.appointment.AppointmentService;
import com.dw.backend.doablewellbeingbackend.business.coach.CoachService;
import com.dw.backend.doablewellbeingbackend.domain.appointment.AppointmentView;
import com.dw.backend.doablewellbeingbackend.domain.appointment.DeclineRequest;
import com.dw.backend.doablewellbeingbackend.domain.coach.CoachCalendarEventView;
import com.dw.backend.doablewellbeingbackend.domain.coach.CoachSummaryResponse;
import com.dw.backend.doablewellbeingbackend.persistence.entity.AppointmentEntity;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/coaches")
@RequiredArgsConstructor
public class CoachController {

    private final CoachService coachService;
    private final AppointmentService appointmentService;
    private final AppointmentMapper appointmentMapper;

    private UUID currentUserId(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }

    @GetMapping
    public List<CoachSummaryResponse> getAllCoaches() {
        return coachService.getAllCoaches();
    }

    ///                         ///
    ///   Appointment Related   ///
    ///                         ///

    @PatchMapping("/me/appointments/{appointmentId}/complete")
    public AppointmentView completeAppointment(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID appointmentId
    ) {
        UUID coachId = currentUserId(jwt);

        AppointmentEntity appt =
                appointmentService.completeAppointment(coachId, appointmentId);

        return appointmentMapper.toView(appt);
    }


    @PreAuthorize("hasRole('coach')")
    @PatchMapping("/me/appointments/{id}/confirm")
    public AppointmentView confirm(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        var coachId = currentUserId(jwt);
        AppointmentEntity appt = appointmentService.confirmAppointment(coachId, id);
        return appointmentMapper.toView(appt);
    }

    @PreAuthorize("hasRole('coach')")
    @PatchMapping("/me/appointments/{id}/decline")
    public AppointmentView decline(@PathVariable UUID id,
                                   @AuthenticationPrincipal Jwt jwt,
                                   @RequestBody DeclineRequest body) {
        var coachId = currentUserId(jwt);
        AppointmentEntity appt = appointmentService.declineAppointment(coachId, id, body.reason());
        return appointmentMapper.toView(appt);
    }

    @PreAuthorize("hasRole('coach')")
    @PatchMapping("/me/appointments/{appointmentId}/cancel")
    public AppointmentView cancel(@PathVariable UUID appointmentId, @AuthenticationPrincipal Jwt jwt) {
        UUID coachId = currentUserId(jwt);
        return appointmentMapper.toView(appointmentService.cancelAppointmentAsCoach(coachId, appointmentId));
    }


    @PreAuthorize("hasRole('coach')")
    @GetMapping("/me/appointments")
    public List<AppointmentView> myCoachAppointments(@AuthenticationPrincipal Jwt jwt) {
        UUID coachId = currentUserId(jwt);
        return appointmentService.getAppointmentsForCoach(coachId)
                .stream()
                .map(appointmentMapper::toView)
                .toList();
    }

    @PreAuthorize("hasRole('coach')")
    @GetMapping("/me/calendar")
    public List<CoachCalendarEventView> myCalendar(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to
    ) {
        UUID coachId = currentUserId(jwt);
        return appointmentService.getCoachCalendar(coachId, from, to);
    }

}
