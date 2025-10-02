package com.dw.doablewellbeingbackend.business.impl.selfReferralUseCaseImpl;

import com.dw.doablewellbeingbackend.business.selfReferralUseCases.GetLatestSelfReferralUseCase;
import com.dw.doablewellbeingbackend.common.exception.NotFoundException;
import com.dw.doablewellbeingbackend.domain.selfreferral.SelfReferralView;
import com.dw.doablewellbeingbackend.presistence.impl.SelfReferralRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetLeatestSelfReferralUseCaseImpl implements GetLatestSelfReferralUseCase {

    private final SelfReferralRepository repo;

    @Override
    public SelfReferralView getLatest(UUID userId){
        var latest = repo.findFirstByUserIdOrderByCreatedAtDesc(userId).orElseThrow(() -> new NotFoundException("No SelfReferral found"));
        return SelfreferralMapper.toView(latest);
    }

}
