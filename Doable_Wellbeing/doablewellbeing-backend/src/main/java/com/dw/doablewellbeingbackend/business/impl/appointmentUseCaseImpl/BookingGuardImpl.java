package com.dw.doablewellbeingbackend.business.impl.appointmentUseCaseImpl;

import com.dw.doablewellbeingbackend.business.appointmentUseCases.BookingGuard;
import com.dw.doablewellbeingbackend.business.selfReferralUseCases.GetSelfReferralStatusUseCase;
import com.dw.doablewellbeingbackend.common.exception.ConflictException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


import java.util.UUID;

@Component
@RequiredArgsConstructor
public class BookingGuardImpl implements BookingGuard {

    private final GetSelfReferralStatusUseCase srStatus;

    @Override
    public void assertUserCanBook(UUID userId){
        var status = srStatus.getStatus(userId);
        if (!status.isHasValidReferral() || status.isNeedsReview()){
            throw new ConflictException("SELF_REFERRAL_REQUIRED");
        }

    }

}
