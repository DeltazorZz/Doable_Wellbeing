package com.dw.doablewellbeingbackend.domain.selfreferral;

import com.dw.doablewellbeingbackend.domain.enums.*;
import lombok.*;

import java.time.LocalDate;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SelfReferralView {

    private Title title;
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private GenderIdentity genderIdentity;
    private Boolean isGenderSameAsAssignedAtBirth;
    private String nhsNumber;

    private String addressLine1;
    private String addressLine2;
    private String addressLine3;
    private String townCity;
    private String county;
    private String postcode;

    private String mobilePhone;
    private String homePhone;
    private String emailAddress;

    private Boolean consentMobileContact;
    private Boolean consentSmsReminders;
    private Boolean consentMobileVoicemail;
    private Boolean consentHomeVoicemail;
    private Boolean consentEmailContact;

    private String presentingProblem;
    private String heardAboutUs;

    private MaritalStatus maritalStatus;
    private AccommodationType accommodationType;
    private EmploymentStatus employmentStatus;
    private SexualOrientation sexualOrientation;
    private EthnicOrigin ethnicOrigin;
    private Religion religion;
    private String firstLanguage;
    private Boolean requiresInterpreter;
    private Boolean englishDifficulty;
    private String englishSupportDetails;
    private Boolean hasDisability;
    private Boolean hasLongTermConditions;
    private Boolean hasArmedForcesAffiliation;
    private Boolean expectingOrChildUnder24m;

    private boolean acceptedPrivacyPolicy;

}
