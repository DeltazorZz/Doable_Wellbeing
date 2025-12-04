package com.dw.backend.doablewellbeingbackend.controller;

import com.dw.backend.doablewellbeingbackend.business.appointment.AppointmentMapper;
import com.dw.backend.doablewellbeingbackend.business.appointment.AppointmentService;
import com.dw.backend.doablewellbeingbackend.domain.appointment.AppointmentView;
import com.dw.backend.doablewellbeingbackend.domain.appointment.BookFromSlotRequest;
import com.dw.backend.doablewellbeingbackend.persistence.entity.AppointmentEntity;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final AppointmentMapper mapper;

    private UUID currentUserId(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }


    @PreAuthorize("hasRole('coach')")
    @PatchMapping("/{id}/confirm")
    public AppointmentView confirm(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        var coachId = currentUserId(jwt);
        AppointmentEntity appt = appointmentService.confirmAppointment(coachId, id);
        return mapper.toView(appt);
    }

    @PreAuthorize("hasRole('coach')")
    @PatchMapping("/{id}/decline")
    public AppointmentView decline(@PathVariable UUID id,
                                   @AuthenticationPrincipal Jwt jwt,
                                   @RequestBody DeclineRequest body) {
        var coachId = currentUserId(jwt);
        AppointmentEntity appt = appointmentService.declineAppointment(id, coachId, body.reason());
        return mapper.toView(appt);
    }

    public record DeclineRequest(String reason) {}

    @PreAuthorize("hasAnyRole('user','client')")
    @PostMapping("/slots/book")
    public AppointmentView bookFromSlot(@Valid @RequestBody BookFromSlotRequest request,
                                        @AuthenticationPrincipal Jwt jwt) {
        UUID clientId = currentUserId(jwt);

        AppointmentEntity appt = appointmentService.requestAppointmentFromSlot(
                request.coachId(),
                clientId,
                request.slotStart(),
                request.durationMinutes(),
                request.notes()
        );

        return mapper.toView(appt);
    }



}
