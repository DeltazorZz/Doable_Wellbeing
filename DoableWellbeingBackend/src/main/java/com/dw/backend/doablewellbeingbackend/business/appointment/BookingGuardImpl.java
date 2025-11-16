package com.dw.backend.doablewellbeingbackend.business.appointment;

import com.dw.backend.doablewellbeingbackend.business.selfReferral.SelfReferralService;
import com.dw.backend.doablewellbeingbackend.common.exception.ConflictException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class BookingGuardImpl implements BookingGuard {

    private final SelfReferralService srStatus;

    @Override
    public void assertUserCanBook(UUID userId){
        var status = srStatus.getStatus(userId);
        if (!status.isHasValidReferral() || status.isNeedsReview()){
            throw new ConflictException("SELF_REFERRAL_REQUIRED");
        }

    }

}
