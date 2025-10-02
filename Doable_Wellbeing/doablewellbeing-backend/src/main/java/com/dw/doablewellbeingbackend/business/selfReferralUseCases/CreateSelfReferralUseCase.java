package com.dw.doablewellbeingbackend.business.selfReferralUseCases;

import com.dw.doablewellbeingbackend.domain.selfreferral.CreateSelfReferralRequest;
import com.dw.doablewellbeingbackend.domain.selfreferral.CreateSelfReferralResponse;

import java.util.UUID;

public interface CreateSelfReferralUseCase {
    CreateSelfReferralResponse create(UUID authenticatedUserId, CreateSelfReferralRequest req);
}
