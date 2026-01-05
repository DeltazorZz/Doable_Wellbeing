package com.dw.backend.doablewellbeingbackend.domain.dashboard;

public record PlacementView(
        int x, int y, int w, int h,
        Integer minW, Integer minH,
        Integer maxW, Integer maxH,
        Boolean isStatic
) {}