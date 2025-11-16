package com.dw.backend.doablewellbeingbackend.business.selfReferral;

import com.dw.backend.doablewellbeingbackend.domain.selfreferral.CreateSelfReferralRequest;
import com.dw.backend.doablewellbeingbackend.domain.selfreferral.CreateSelfReferralResponse;
import com.dw.backend.doablewellbeingbackend.domain.selfreferral.SelfReferralStatusResponse;
import com.dw.backend.doablewellbeingbackend.domain.selfreferral.SelfReferralView;

import java.util.UUID;

public interface SelfReferralService {

    //Create self-referral
    CreateSelfReferralResponse create(UUID authenticatedUserId, CreateSelfReferralRequest req);

    // Get latest self-referral
    SelfReferralView getLatest(UUID userId);


    //Get self-referral status
    SelfReferralStatusResponse getStatus(UUID userId);

}
