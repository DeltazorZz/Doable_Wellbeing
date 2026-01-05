package com.dw.backend.doablewellbeingbackend.controller;
import com.dw.backend.doablewellbeingbackend.business.dashboard.DashboardService;
import com.dw.backend.doablewellbeingbackend.domain.dashboard.CreateDashboardWidgetRequest;
import com.dw.backend.doablewellbeingbackend.domain.dashboard.DashboardView;
import com.dw.backend.doablewellbeingbackend.domain.dashboard.UpdatePlacementsRequest;
import com.dw.backend.doablewellbeingbackend.domain.dashboard.UpdateWidgetSettingsRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/dashboards")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    private UUID currentUserId(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }

    @PreAuthorize("hasAnyRole('user','client')")
    @GetMapping("/default")
    public DashboardView getDefault(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = currentUserId(jwt);
        return dashboardService.getOrCreateDefaultDashboard(userId);
    }

    @PreAuthorize("hasAnyRole('user','client')")
    @PostMapping("/{dashboardId}/widgets")
    public Map<String, Object> addWidget(
            @PathVariable UUID dashboardId,
            @Valid @RequestBody CreateDashboardWidgetRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userId = currentUserId(jwt);
        UUID widgetId = dashboardService.addWidget(userId, dashboardId, request);
        return Map.of("widgetId", widgetId);
    }

    @PreAuthorize("hasAnyRole('user','client')")
    @PutMapping("/{dashboardId}/placements")
    public Map<String, Object> updatePlacements(
            @PathVariable UUID dashboardId,
            @Valid @RequestBody UpdatePlacementsRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        dashboardService.upsertPlacements(currentUserId(jwt), dashboardId, request);
        return Map.of("status", "ok");
    }

    @PreAuthorize("hasAnyRole('user','client')")
    @PutMapping("/{dashboardId}/widgets/{widgetId}/settings")
    public Map<String, Object> updateWidgetSettings(
            @PathVariable UUID dashboardId,
            @PathVariable UUID widgetId,
            @Valid @RequestBody UpdateWidgetSettingsRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        dashboardService.updateWidgetSettings(currentUserId(jwt), dashboardId, widgetId, request);
        return Map.of("status", "ok");
    }

    @PreAuthorize("hasAnyRole('user','client')")
    @DeleteMapping("/{dashboardId}/widgets/{widgetId}")
    public Map<String, Object> deleteWidget(
            @PathVariable UUID dashboardId,
            @PathVariable UUID widgetId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        dashboardService.deleteWidget(currentUserId(jwt), dashboardId, widgetId);
        return Map.of("status", "ok");
    }
}
