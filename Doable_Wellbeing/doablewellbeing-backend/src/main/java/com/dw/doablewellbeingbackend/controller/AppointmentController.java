package com.dw.doablewellbeingbackend.controller;

import com.dw.doablewellbeingbackend.business.appointmentUseCases.AddSessionNoteUseCase;
import com.dw.doablewellbeingbackend.business.appointmentUseCases.ChangeAppointmentStatusUseCase;
import com.dw.doablewellbeingbackend.business.appointmentUseCases.CreateAppointmentUseCase;
import com.dw.doablewellbeingbackend.business.appointmentUseCases.GetAppointmentUseCase;
import com.dw.doablewellbeingbackend.domain.appointment.AppointmentView;
import com.dw.doablewellbeingbackend.domain.appointment.CreateAppointmentRequest;
import com.dw.doablewellbeingbackend.domain.appointment.SessionNoteRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final CreateAppointmentUseCase createUseCase;
    private final GetAppointmentUseCase getUseCase;
    private final ChangeAppointmentStatusUseCase changeUseCase;
    private final AddSessionNoteUseCase noteUseCase;

    @PostMapping
    public AppointmentView create(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody CreateAppointmentRequest req) {
        UUID clientId = UUID.fromString(jwt.getSubject());
        return createUseCase.create(clientId, req);
    }

    @GetMapping("/me")
    public List<AppointmentView> myAppointments(@AuthenticationPrincipal Jwt jwt){
        UUID clientId = UUID.fromString(jwt.getSubject());
        return getUseCase.forClient(clientId);
    }

    @PostMapping("/{id}/cancel")
    public void clientCancel(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id){
        UUID clientId = UUID.fromString(jwt.getSubject());
        changeUseCase.clientCancel(clientId, id);
    }

    @PostMapping("/{id}/cancelbycoach")
    public void coachCancel(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id){
        UUID clientId = UUID.fromString(jwt.getSubject());
        changeUseCase.clientCancel(clientId, id);
    }

    @PostMapping("/{id}/addnotes")
    public void addNotes(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id, @RequestBody SessionNoteRequest req){
        UUID coachId =  UUID.fromString(jwt.getSubject());
        noteUseCase.addNote(coachId, id, req.getNote());
    }


}
