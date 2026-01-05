package com.dw.backend.doablewellbeingbackend.domain.dashboard;


import com.fasterxml.jackson.databind.JsonNode;

import java.util.Map;
import java.util.UUID;

public record DashboardWidgetView(
        UUID id,
        String moduleCode,
        String title,
        JsonNode settings,
        boolean isActive,
        Map<String, PlacementView> placements
) {}