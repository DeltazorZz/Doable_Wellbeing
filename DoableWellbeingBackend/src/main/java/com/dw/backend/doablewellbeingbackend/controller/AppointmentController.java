package com.dw.backend.doablewellbeingbackend.controller;


import com.dw.backend.doablewellbeingbackend.business.appointment.AppointmentService;
import com.dw.backend.doablewellbeingbackend.domain.appointment.AppointmentView;
import com.dw.backend.doablewellbeingbackend.domain.appointment.CreateAppointmentRequest;
import com.dw.backend.doablewellbeingbackend.domain.appointment.SessionNoteRequest;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService  appointmentService;

    @PostMapping
    public AppointmentView create(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody CreateAppointmentRequest req) {
        UUID clientId = UUID.fromString(jwt.getSubject());
        return appointmentService.create(clientId, req);
    }

    @GetMapping("/me")
    public List<AppointmentView> myAppointments(@AuthenticationPrincipal Jwt jwt){
        UUID clientId = UUID.fromString(jwt.getSubject());
        return appointmentService.forClient(clientId);
    }

    @PostMapping("/{id}/cancel")
    public void clientCancel(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id){
        UUID clientId = UUID.fromString(jwt.getSubject());
        appointmentService.clientCancel(clientId, id);
    }

    @PostMapping("/{id}/cancelbycoach")
    public void coachCancel(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id){
        UUID clientId = UUID.fromString(jwt.getSubject());
        appointmentService.clientCancel(clientId, id);
    }

    @PostMapping("/{id}/addnotes")
    public void addNotes(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id, @RequestBody SessionNoteRequest req){
        UUID coachId =  UUID.fromString(jwt.getSubject());
        appointmentService.addNote(coachId, id, req.getNote());
    }


}
