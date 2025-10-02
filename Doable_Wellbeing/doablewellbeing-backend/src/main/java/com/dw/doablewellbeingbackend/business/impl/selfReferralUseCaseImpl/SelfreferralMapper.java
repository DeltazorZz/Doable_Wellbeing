package com.dw.doablewellbeingbackend.business.impl.selfReferralUseCaseImpl;

import com.dw.doablewellbeingbackend.domain.selfreferral.SelfReferralView;
import com.dw.doablewellbeingbackend.presistence.entity.SelfReferralEntity;

final class SelfreferralMapper {
    static SelfReferralView toView(SelfReferralEntity e){
        return SelfReferralView.builder()
                .title(e.getTitle())
                .firstName(e.getFirstName())
                .lastName(e.getLastName())
                .dateOfBirth(e.getDateOfBirth())
                .genderIdentity(e.getGenderIdentity())
                .isGenderSameAsAssignedAtBirth(e.getIsGenderSameAsAssignedAtBirth())
                .nhsNumber(e.getNhsNumber())
                .addressLine1(e.getAddressLine1())
                .addressLine2(e.getAddressLine2())
                .addressLine3(e.getAddressLine3())
                .townCity(e.getTownCity())
                .county(e.getCounty())
                .postcode(e.getPostcode())
                .mobilePhone(e.getMobilePhone())
                .homePhone(e.getHomePhone())
                .emailAddress(e.getEmailAddress())
                .consentMobileContact(e.getConsentMobileContact())
                .consentSmsReminders(e.getConsentSmsReminders())
                .consentMobileVoicemail(e.getConsentMobileVoicemail())
                .consentHomeVoicemail(e.getConsentHomeVoicemail())
                .consentEmailContact(e.getConsentEmailContact())
                .presentingProblem(e.getPresentingProblem())
                .heardAboutUs(e.getHeardAboutUs())
                .maritalStatus(e.getMaritalStatus())
                .accommodationType(e.getAccommodationType())
                .employmentStatus(e.getEmploymentStatus())
                .sexualOrientation(e.getSexualOrientation())
                .ethnicOrigin(e.getEthnicOrigin())
                .religion(e.getReligion())
                .firstLanguage(e.getFirstLanguage())
                .requiresInterpreter(e.getRequiresInterpreter())
                .englishDifficulty(e.getEnglishDifficulty())
                .englishSupportDetails(e.getEnglishSupportDetails())
                .hasDisability(e.getHasDisability())
                .hasLongTermConditions(e.getHasLongTermConditions())
                .hasArmedForcesAffiliation(e.getHasArmedForcesAffiliation())
                .expectingOrChildUnder24m(e.getExpectingOrChildUnder24m())
                .acceptedPrivacyPolicy(e.isAcceptedPrivacyPolicy())
                .build();
    }
}
