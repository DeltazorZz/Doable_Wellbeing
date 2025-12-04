package com.dw.backend.doablewellbeingbackend.controller;

import com.dw.backend.doablewellbeingbackend.business.appointment.AppointmentService;
import com.dw.backend.doablewellbeingbackend.business.availability.CoachAvailabilityService;
import com.dw.backend.doablewellbeingbackend.business.user.UserService;
import com.dw.backend.doablewellbeingbackend.domain.appointment.CoachAvailabilityResponse;
import com.dw.backend.doablewellbeingbackend.domain.user.User;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@PreAuthorize("hasRole('admin')")
@RestController
@RequestMapping("/actions")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final CoachAvailabilityService availabilityService;



    // User related actions
    @GetMapping("/active")
    public Page<User> listActive(@RequestParam(defaultValue = "0")int page, @RequestParam(defaultValue = "20")int size){
        return userService.getAllActive(PageRequest.of(page, size));
    }

    @GetMapping("/all")
    public Page<User> listAll(@RequestParam(defaultValue = "0")int page, @RequestParam(defaultValue = "20")int size){
        return userService.getAll(PageRequest.of(page, size));
    }


    // Coach related actions
    @GetMapping("/{coachId}")
    public List<CoachAvailabilityResponse> getAvailabilitiesForCoach(
            @PathVariable UUID coachId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return availabilityService.getAvailabilitiesForCoach(coachId, from, to);
    }

}
