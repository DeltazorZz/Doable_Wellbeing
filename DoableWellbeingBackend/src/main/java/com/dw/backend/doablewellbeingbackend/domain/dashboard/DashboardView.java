package com.dw.backend.doablewellbeingbackend.domain.dashboard;

import java.util.List;
import java.util.UUID;

public record DashboardView(
        UUID dashboardId,
        String name,
        boolean isDefault,
        List<DashboardWidgetView> widgets
) {}