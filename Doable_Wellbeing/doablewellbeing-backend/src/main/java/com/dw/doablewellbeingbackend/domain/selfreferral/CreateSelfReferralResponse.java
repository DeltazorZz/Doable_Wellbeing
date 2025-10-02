package com.dw.doablewellbeingbackend.domain.selfreferral;

import lombok.*;

import java.util.UUID;

@Data @AllArgsConstructor @NoArgsConstructor @Builder
public class CreateSelfReferralResponse {
    private UUID referralId;
    private UUID userId;
}
