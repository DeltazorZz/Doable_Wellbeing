package com.dw.backend.doablewellbeingbackend.business.selfReferral;


import com.dw.backend.doablewellbeingbackend.domain.selfreferral.CreateSelfReferralRequest;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class ReferralValidator {

    private static final Pattern UK_POSTCODE = Pattern.compile( "^[A-Z]{1,2}\\d[A-Z\\d]? ?\\d[A-Z]{2}$",  Pattern.CASE_INSENSITIVE );

    public void validateRequest(CreateSelfReferralRequest r, String currentUserEmail){
        if(!UK_POSTCODE.matcher(r.getPostcode().trim()).matches())
            throw new IllegalArgumentException("Invalid UK postcode format");

            if (!r.getEmailAddress().equalsIgnoreCase(currentUserEmail)){
                throw new IllegalArgumentException("Email change is not allowed in self-referral. Update your profile first.");
        }
    }
}
