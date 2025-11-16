package com.dw.backend.doablewellbeingbackend.domain.selfreferral;

import com.dw.backend.doablewellbeingbackend.domain.enums.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data @AllArgsConstructor
@NoArgsConstructor @Builder
public class CreateSelfReferralRequest {

    //Required
    @NotNull
    private Title title;
    @NotBlank private String firstName;
    @NotBlank private String lastName;
    @NotNull private LocalDate dateOfBirth;
    @NotNull private GenderIdentity genderIdentity;
    @NotNull private Boolean isGenderSameAsAssignedAtBirth;
    private String nhsNumber;

    //Address
    @NotBlank private String addressLine1;
    private String addressLine2;
    private String addressLine3;
    @NotBlank private String townCity;
    private String county;
    @NotBlank private String postcode;

    //Contacts
    @NotBlank @Pattern(regexp = "^[0-9+ ]{6,20}$") private String mobilePhone;
    private String homePhone;
    @NotBlank @Email private String emailAddress;

    //Consents
    @NotNull private Boolean consentMobileContact;
    @NotNull private Boolean consentSmsReminders;
    @NotNull private Boolean consentMobileVoicemail;
    @NotNull private Boolean consentHomeVoicemail;
    @NotNull private Boolean consentEmailContact;

    //Presenting
    @NotBlank private String presentingProblem;
    private String heardAboutUs;

    //About you - All Required for now
    @NotNull private MaritalStatus maritalStatus;
    @NotNull private AccommodationType accommodationType;
    @NotNull private EmploymentStatus employmentStatus;
    @NotNull private SexualOrientation sexualOrientation;
    @NotNull private EthnicOrigin ethnicOrigin;
    @NotNull private Religion religion;
    @NotBlank private String firstLanguage;
    @NotNull private Boolean requiresInterpreter;
    @NotNull private Boolean englishDifficulty;
    private String englishSupportDetails;
    @NotNull private Boolean hasDisability;
    @NotNull private Boolean hasLongTermConditions;
    @NotNull private Boolean hasArmedForcesAffiliation;
    @NotNull private Boolean expectingOrChildUnder24m;

    @AssertTrue(message = "Privacy policy must be accepted")
    private boolean acceptedPrivacyPolicy;

}
