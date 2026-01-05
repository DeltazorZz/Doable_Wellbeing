package com.dw.backend.doablewellbeingbackend.controller;

import com.dw.backend.doablewellbeingbackend.business.appointment.AppointmentMapper;
import com.dw.backend.doablewellbeingbackend.business.appointment.AppointmentService;
import com.dw.backend.doablewellbeingbackend.domain.appointment.AppointmentView;
import com.dw.backend.doablewellbeingbackend.domain.appointment.BookFromSlotRequest;
import com.dw.backend.doablewellbeingbackend.domain.appointment.InstantBookRequest;
import com.dw.backend.doablewellbeingbackend.domain.dashboard.AddAppointmentNoteRequest;
import com.dw.backend.doablewellbeingbackend.persistence.entity.AppointmentEntity;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
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



    @PreAuthorize("hasAnyRole('user','client')")
    @PostMapping("/slots/book")
    public AppointmentView bookFromSlot(@Valid @RequestBody BookFromSlotRequest request, @AuthenticationPrincipal Jwt jwt) {
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

    @PreAuthorize("hasAnyRole('user','client')")
    @PostMapping("/dev/instant-book")
    public AppointmentView instantBookFromSlot(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody @Valid InstantBookRequest request
    ) {
        UUID clientId = currentUserId(jwt);

        var appt = appointmentService.instantBookFromSlot(
                request.coachId(),
                clientId,
                request.slotStart(),
                request.durationMinutes(),
                request.notes()
        );

        return mapper.toView(appt);
    }

    @PreAuthorize("hasAnyRole('coach','admin')")
    @PostMapping("/{appointmentId}/notes")
    public Map<String,Object> addNote(
            @PathVariable UUID appointmentId,
            @RequestBody AddAppointmentNoteRequest req,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID coachId = UUID.fromString(jwt.getSubject());
        appointmentService.addNote(coachId, appointmentId, req);
        return Map.of("status","ok");
    }



}
