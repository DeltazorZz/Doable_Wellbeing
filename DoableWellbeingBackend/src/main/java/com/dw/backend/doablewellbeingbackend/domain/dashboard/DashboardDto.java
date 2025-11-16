package com.dw.backend.doablewellbeingbackend.domain.dashboard;

import java.util.List;

public record DashboardDto(
        String name,
        LayoutsDto layouts,
        List<WidgetDto> widgets
) {}