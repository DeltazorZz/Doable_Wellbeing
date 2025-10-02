package com.dw.doablewellbeingbackend.business.selfReferralUseCases;

import com.dw.doablewellbeingbackend.domain.selfreferral.SelfReferralView;

import java.util.UUID;

public interface GetLatestSelfReferralUseCase {
    SelfReferralView getLatest(UUID userId);
}
