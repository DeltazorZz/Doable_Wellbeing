package com.dw.backend.doablewellbeingbackend.domain.dashboard;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotNull;

public record UpdateWidgetSettingsRequest(
        @NotNull JsonNode settings
) {}