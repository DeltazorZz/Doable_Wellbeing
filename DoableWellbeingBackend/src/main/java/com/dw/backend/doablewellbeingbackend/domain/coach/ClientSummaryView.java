package com.dw.backend.doablewellbeingbackend.domain.coach;

import java.util.UUID;

public record ClientSummaryView(
        UUID id,
        String name,
        String email
) {}