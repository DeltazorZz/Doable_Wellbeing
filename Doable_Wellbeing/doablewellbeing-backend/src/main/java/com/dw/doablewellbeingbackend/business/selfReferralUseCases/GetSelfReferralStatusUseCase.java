package com.dw.doablewellbeingbackend.business.selfReferralUseCases;

import com.dw.doablewellbeingbackend.domain.selfreferral.SelfReferralStatusResponse;

import java.util.UUID;

public interface GetSelfReferralStatusUseCase {
    SelfReferralStatusResponse getStatus(UUID userId);
}
