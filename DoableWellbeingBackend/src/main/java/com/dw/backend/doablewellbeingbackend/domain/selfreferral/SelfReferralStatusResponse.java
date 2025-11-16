package com.dw.backend.doablewellbeingbackend.domain.selfreferral;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data @AllArgsConstructor @NoArgsConstructor @Builder
public class SelfReferralStatusResponse {

    private boolean hasValidReferral;
    private UUID lastReferralId;
    private OffsetDateTime lastUpdatedAt;
    private boolean needsReview;


}
