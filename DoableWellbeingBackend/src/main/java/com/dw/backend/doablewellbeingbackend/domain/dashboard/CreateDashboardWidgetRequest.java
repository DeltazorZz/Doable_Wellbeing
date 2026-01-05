package com.dw.backend.doablewellbeingbackend.domain.dashboard;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;

public record CreateDashboardWidgetRequest(
        @NotBlank String moduleCode,
        String title,
        JsonNode settings,
        Integer x, Integer y, Integer w, Integer h,
        String breakpoint
) {}
