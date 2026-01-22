package com.dw.backend.doablewellbeingbackend.controller;

import com.dw.backend.doablewellbeingbackend.business.appointment.AppointmentMapper;
import com.dw.backend.doablewellbeingbackend.business.appointment.AppointmentService;
import com.dw.backend.doablewellbeingbackend.business.user.UserService;
import com.dw.backend.doablewellbeingbackend.domain.appointment.AppointmentView;
import com.dw.backend.doablewellbeingbackend.domain.user.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@PreAuthorize("hasAnyRole('user', 'client')")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {


    private final UserService userService;
    private final AppointmentService appointmentService;
    private final AppointmentMapper appointmentMapper;


    private UUID currentUserId(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }

    @PutMapping("/{id}")
    public void update(@AuthenticationPrincipal Jwt jwt, @RequestBody UpdateUserRequest req){
        UUID id = currentUserId(jwt);
        userService.update(id, req);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@AuthenticationPrincipal Jwt jwt){
        UUID id = currentUserId(jwt);
        userService.delete(id);
    }

    @GetMapping("/me")
    public Map<String, Object> me(@AuthenticationPrincipal Jwt principal) {
        if(principal == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        UUID userId = UUID.fromString(principal.getSubject());
        var user = userService.getRequired(userId);
        return  Map.of(
                "userId", user.getId(),
                "email", user.getEmail(),
                "firstName", user.getFirstName(),
                "lastName", user.getLastName(),
                "roles", user.getRoleNames()
        );
    }


    @PatchMapping("/appointment/{id}/cancel")
    public AppointmentView cancelMyAppointment(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID clientId = currentUserId(jwt);
        var appt = appointmentService.cancelAppointmentAsClient(clientId, id);
        return appointmentMapper.toView(appt);
    }


}
