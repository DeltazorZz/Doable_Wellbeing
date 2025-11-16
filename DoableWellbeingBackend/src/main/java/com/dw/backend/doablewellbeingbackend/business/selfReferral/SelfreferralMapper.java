package com.dw.backend.doablewellbeingbackend.business.selfReferral;

import com.dw.backend.doablewellbeingbackend.domain.selfreferral.SelfReferralView;
import com.dw.backend.doablewellbeingbackend.presistence.entity.SelfReferralEntity;

final class SelfreferralMapper {
    static SelfReferralView toView(SelfReferralEntity e){
        return SelfReferralView.builder()
                .title(e.getTitle())
                .genderIdentity(e.getGenderIdentity())
                .isGenderSameAsAssignedAtBirth(e.getIsGenderSameAsAssignedAtBirth())
                .mobilePhone(e.getMobilePhone())
                .homePhone(e.getHomePhone())
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
                .build();
    }
}
