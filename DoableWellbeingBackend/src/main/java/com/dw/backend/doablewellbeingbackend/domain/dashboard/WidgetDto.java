package com.dw.backend.doablewellbeingbackend.domain.dashboard;

import java.util.UUID;

public record WidgetDto(
        UUID id, String type, String title, String settings // settings JSON string
) {}
