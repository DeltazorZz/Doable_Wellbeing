package com.dw.backend.doablewellbeingbackend.domain.dashboard;

import java.util.UUID;

public record ModuleView(
        UUID id,
        String code,
        String name,
        String description
) {}