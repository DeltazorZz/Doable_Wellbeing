package com.dw.backend.doablewellbeingbackend.controller;

import com.dw.backend.doablewellbeingbackend.business.availability.CoachAvailabilityService;
import com.dw.backend.doablewellbeingbackend.business.availability.SlotService;
import com.dw.backend.doablewellbeingbackend.domain.appointment.CoachAvailabilityRequest;
import com.dw.backend.doablewellbeingbackend.domain.appointment.CoachAvailabilityResponse;
import com.dw.backend.doablewellbeingbackend.domain.appointment.CoachAvailabilityView;
import com.dw.backend.doablewellbeingbackend.domain.appointment.SlotView;
import com.dw.backend.doablewellbeingbackend.persistence.impl.CoachRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/coach/availabilities")
@RequiredArgsConstructor
public class CoachAvailabilityController {

    private final CoachAvailabilityService availabilityService;
    private final CoachRepository coachRepository;
    private final SlotService slotService;

    private UUID currentUserId(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }
    @PreAuthorize("hasRole('coach')")
    @PostMapping("/me")
    public List<CoachAvailabilityView> createAvailabilityForMe(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CoachAvailabilityRequest request
    ) {
        UUID coachId = currentUserId(jwt);
        return availabilityService.createAvailability(coachId, request);
    }

    @PreAuthorize("hasRole('admin')")
    @PostMapping("/{coachId}")
    public List<CoachAvailabilityView> createAvailabilityForCoach(
            @PathVariable UUID coachId,
            @Valid @RequestBody CoachAvailabilityRequest request
    ) {
        return availabilityService.createAvailability(coachId, request);
    }

    @PreAuthorize("hasRole('coach')")
    @GetMapping("/me")
    public List<CoachAvailabilityResponse> getMyAvailabilities(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        UUID coachId = currentUserId(jwt);
        return availabilityService.getAvailabilitiesForCoach(coachId, from, to);
    }

    @PreAuthorize("hasRole('coach')")
    @PutMapping("/{availabilityId}/day-off")
    public CoachAvailabilityView setDayOff(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID availabilityId) {
        UUID coachId = currentUserId(jwt);
        return availabilityService.setDayOff(coachId, availabilityId);
    }

    @PreAuthorize("hasRole('coach')")
    @DeleteMapping("/me/{availabilityId}")
    public void deleteMyAvailability(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID availabilityId
    ) {
        UUID coachId = currentUserId(jwt);
        availabilityService.deleteAvailability(coachId, availabilityId);
    }

    @PreAuthorize("permitAll()")
    @GetMapping("/{coachId}/slots")
    public List<SlotView> getSlotsForCoach(
            @PathVariable UUID coachId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "60") int slotLengthMinutes
    ) {
        var coach = coachRepository.findById(coachId)
                .orElseThrow(() -> new IllegalArgumentException("Coach not found"));


        ZoneId coachZone = ZoneId.of("Europe/London");

        return slotService.getSlotsForCoach(
                coachId,
                from,
                to,
                slotLengthMinutes,
                coachZone
        );
    }
}
