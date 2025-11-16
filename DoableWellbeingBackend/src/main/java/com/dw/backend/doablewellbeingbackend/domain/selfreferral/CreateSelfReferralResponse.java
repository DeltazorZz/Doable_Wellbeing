package com.dw.backend.doablewellbeingbackend.domain.selfreferral;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data @AllArgsConstructor @NoArgsConstructor @Builder
public class CreateSelfReferralResponse {
    private UUID referralId;
    private UUID userId;
}
