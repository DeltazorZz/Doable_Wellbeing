package com.dw.backend.doablewellbeingbackend.domain.dashboard;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record PlacementUpsertRequest(
        @NotNull UUID widgetId,
        @NotBlank String breakpoint,
        @Min(0) int x,
        @Min(0) int y,
        @Min(1) int w,
        @Min(1) int h,
        Integer minW,
        Integer minH,
        Integer maxW,
        Integer maxH,
        Boolean isStatic
) {}