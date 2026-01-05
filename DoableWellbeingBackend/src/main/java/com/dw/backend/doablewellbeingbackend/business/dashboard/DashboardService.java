package com.dw.backend.doablewellbeingbackend.business.dashboard;

import com.dw.backend.doablewellbeingbackend.domain.dashboard.CreateDashboardWidgetRequest;
import com.dw.backend.doablewellbeingbackend.domain.dashboard.DashboardView;
import com.dw.backend.doablewellbeingbackend.domain.dashboard.UpdatePlacementsRequest;
import com.dw.backend.doablewellbeingbackend.domain.dashboard.UpdateWidgetSettingsRequest;

import java.util.UUID;

public interface DashboardService {
    DashboardView getOrCreateDefaultDashboard(UUID userId);

    UUID addWidget(UUID userId, UUID dashboardId, CreateDashboardWidgetRequest request);

    void upsertPlacements(UUID userId, UUID dashboardId, UpdatePlacementsRequest request);

    void updateWidgetSettings(UUID userId, UUID dashboardId, UUID widgetId, UpdateWidgetSettingsRequest request);

    void deleteWidget(UUID userId, UUID dashboardId, UUID widgetId);
}