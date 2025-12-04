package com.dw.backend.doablewellbeingbackend.domain.coach;

import java.util.UUID;

public record CoachSummaryResponse(
        UUID id,
        String displayName,
        String bio,
        String expertise,
        String timezone
) {
}
