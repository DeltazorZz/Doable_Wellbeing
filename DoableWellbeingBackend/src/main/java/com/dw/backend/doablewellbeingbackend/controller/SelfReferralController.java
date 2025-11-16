package com.dw.backend.doablewellbeingbackend.controller;

//import com.dw.doablewellbeingbackend.business.selfReferralUseCases.CreateSelfReferralUseCase;

import com.dw.backend.doablewellbeingbackend.business.selfReferral.SelfReferralService;
import com.dw.backend.doablewellbeingbackend.domain.selfreferral.CreateSelfReferralRequest;
import com.dw.backend.doablewellbeingbackend.domain.selfreferral.CreateSelfReferralResponse;
import com.dw.backend.doablewellbeingbackend.domain.selfreferral.SelfReferralStatusResponse;
import com.dw.backend.doablewellbeingbackend.domain.selfreferral.SelfReferralView;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/me/self-referral")
@RequiredArgsConstructor
public class SelfReferralController {

    private final SelfReferralService selfrefService;


    @GetMapping("/status")
    public SelfReferralStatusResponse status(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject()); // vagy: UUID.fromString(jwt.getClaimAsString("user_id"))
        return selfrefService.getStatus(userId);
    }

    @GetMapping
    public SelfReferralView latest(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return selfrefService.getLatest(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateSelfReferralResponse create(@AuthenticationPrincipal Jwt jwt,
        @Valid @RequestBody CreateSelfReferralRequest req) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return selfrefService.create(userId, req);
    }
}
