package com.dw.doablewellbeingbackend.business.impl.selfReferralUseCaseImpl;

import com.dw.doablewellbeingbackend.business.selfReferralUseCases.GetSelfReferralStatusUseCase;
import com.dw.doablewellbeingbackend.domain.selfreferral.SelfReferralStatusResponse;
import com.dw.doablewellbeingbackend.presistence.impl.SelfReferralRepository;
import com.dw.doablewellbeingbackend.presistence.impl.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.Period;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetSelfReferralStatusUseCaseImpl implements GetSelfReferralStatusUseCase {

    private SelfReferralRepository selfRepo;
    private UserRepository userRepo;

    private static final int MONTHS_VALID = 6;

    @Override
    public SelfReferralStatusResponse getStatus(UUID userId){
        var latest = selfRepo.findFirstByUserIdOrderByCreatedAtDesc(userId).orElse(null);
        if(latest == null || !latest.isAcceptedPrivacyPolicy()){
            return SelfReferralStatusResponse.builder().hasValidReferral(false).needsReview(true).build();
        }
        var months = Period.between(latest.getCreatedAt().toLocalDate(), OffsetDateTime.now().toLocalDate()).getMonths();
        boolean fresh = months < MONTHS_VALID;
        return SelfReferralStatusResponse.builder().hasValidReferral(true).lastReferralId(latest.getId()).lastUpdatedAt(latest.getCreatedAt()).needsReview(!fresh).build();
    }

}
