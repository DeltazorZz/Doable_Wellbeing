package com.dw.doablewellbeingbackend.domain.selfreferral;


import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data @AllArgsConstructor @NoArgsConstructor @Builder
public class SelfReferralStatusResponse {

    private boolean hasValidReferral;
    private UUID lastReferralId;
    private OffsetDateTime lastUpdatedAt;
    private boolean needsReview;


}
