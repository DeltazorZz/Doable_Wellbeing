package com.dw.doablewellbeingbackend.controller;

//import com.dw.doablewellbeingbackend.business.selfReferralUseCases.CreateSelfReferralUseCase;
import com.dw.doablewellbeingbackend.business.selfReferralUseCases.CreateSelfReferralUseCase;
import com.dw.doablewellbeingbackend.business.selfReferralUseCases.GetLatestSelfReferralUseCase;
import com.dw.doablewellbeingbackend.business.selfReferralUseCases.GetSelfReferralStatusUseCase;
import com.dw.doablewellbeingbackend.domain.selfreferral.CreateSelfReferralRequest;
import com.dw.doablewellbeingbackend.domain.selfreferral.CreateSelfReferralResponse;
import com.dw.doablewellbeingbackend.domain.selfreferral.SelfReferralStatusResponse;
import com.dw.doablewellbeingbackend.domain.selfreferral.SelfReferralView;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/me/self-referral")
@RequiredArgsConstructor
public class SelfReferralController {

    private final GetSelfReferralStatusUseCase statusUseCase;
    private final GetLatestSelfReferralUseCase latestUseCase;
    private final CreateSelfReferralUseCase createUseCase;

    @GetMapping("/status")
    public SelfReferralStatusResponse status(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject()); // vagy: UUID.fromString(jwt.getClaimAsString("user_id"))
        return statusUseCase.getStatus(userId);
    }

    @GetMapping
    public SelfReferralView latest(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return latestUseCase.getLatest(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateSelfReferralResponse create(@AuthenticationPrincipal Jwt jwt,
        @Valid @RequestBody CreateSelfReferralRequest req) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return createUseCase.create(userId, req);
    }
}
