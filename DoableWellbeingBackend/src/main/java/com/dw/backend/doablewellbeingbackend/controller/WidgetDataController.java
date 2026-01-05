package com.dw.backend.doablewellbeingbackend.controller;

import com.dw.backend.doablewellbeingbackend.business.dashboard.HabitService;
import com.dw.backend.doablewellbeingbackend.business.dashboard.MoodLogService;
import com.dw.backend.doablewellbeingbackend.business.dashboard.WidgetDataService;
import com.dw.backend.doablewellbeingbackend.domain.dashboard.CreateHabitRequest;
import com.dw.backend.doablewellbeingbackend.domain.dashboard.LogMoodRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/widgets")
public class WidgetDataController {
    private final WidgetDataService widgetDataService;
    private final MoodLogService moodLogService;
    private final HabitService habitService;


    private UUID currentUserId(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }

    @PreAuthorize("hasAnyRole('user','client')")
    @GetMapping("/{widgetId}/data")
    public Object getWidgetData(
            @PathVariable UUID widgetId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return widgetDataService.getWidgetData(currentUserId(jwt), widgetId);
    }

    @PreAuthorize("hasAnyRole('user','client')")
    @PostMapping("/habits")
    public Map<String,Object> createHabit(@RequestBody CreateHabitRequest req, @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        UUID id = habitService.createHabit(userId, req);
        return Map.of("habitId", id);
    }

    @PreAuthorize("hasAnyRole('user','client')")
    @PostMapping("/habits/{habitId}/done")
    public Map<String,Object> markDone(@PathVariable UUID habitId, @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        habitService.markDoneToday(userId, habitId);
        return Map.of("status","ok");
    }



    //        //
    // LOGGER //
    //        //
    @PreAuthorize("hasAnyRole('user','client')")
    @PostMapping("/mood")
    public Map<String,Object> logMood(@Valid @RequestBody LogMoodRequest req, @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        moodLogService.log(userId, req);
        return Map.of("status","ok");
    }
}
