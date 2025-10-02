package com.dw.doablewellbeingbackend.presistence.entity;

import com.dw.doablewellbeingbackend.domain.enums.*;
import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.time.LocalDate;
import java.util.UUID;


@Entity
@Table(name = "self_referrals")
@Data @Builder
@NoArgsConstructor
@AllArgsConstructor
public class SelfReferralEntity {
    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @PrePersist
    void pre(){if (id == null){id = UUID.randomUUID();}}

    @Column(name = "user_id", nullable = false, columnDefinition = "uuid")
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "title")
    private Title title;
    @Column(name = "first_name") private String firstName;
    @Column(name = "last_name")  private String lastName;
    @Column(name = "date_of_birth") private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender_identity")
    private GenderIdentity genderIdentity;

    @Column(name = "is_gender_same_as_assigned_at_birth") private Boolean isGenderSameAsAssignedAtBirth;
    @Column(name = "nhs_number") private String nhsNumber;

    @Column(name = "address_line1") private String addressLine1;
    @Column(name = "address_line2") private String addressLine2;
    @Column(name = "address_line3") private String addressLine3;
    @Column(name = "town_city")    private String townCity;
    private String county;
    @Column(name = "postcode")
    private String postcode;

    @Column(name = "mobile_phone") private String mobilePhone;
    @Column(name = "home_phone")   private String homePhone;
    @Column(name = "email_address") private String emailAddress;
    @Column(name = "consent_mobile_contact") private Boolean consentMobileContact;
    @Column(name = "consent_sms_reminders")  private Boolean consentSmsReminders;
    @Column(name = "consent_mobile_voicemail") private Boolean consentMobileVoicemail;
    @Column(name = "consent_home_voicemail")   private Boolean consentHomeVoicemail;
    @Column(name = "consent_email_contact")    private Boolean consentEmailContact;

    @Column(name = "presenting_problem", columnDefinition = "text")
    private String presentingProblem;
    @Column(name = "heard_about_us")
    private String heardAboutUs;

    @Enumerated(EnumType.STRING)
    @Column(name = "marital_status") private MaritalStatus maritalStatus;
    @Enumerated(EnumType.STRING)
    @Column(name = "accommodation_type") private AccommodationType accommodationType;
    @Enumerated(EnumType.STRING)
    @Column(name = "employment_status") private EmploymentStatus employmentStatus;
    @Enumerated(EnumType.STRING)
    @Column(name = "sexual_orientation") private SexualOrientation sexualOrientation;
    @Enumerated(EnumType.STRING)
    @Column(name = "ethnic_origin") private EthnicOrigin ethnicOrigin;
    @Enumerated(EnumType.STRING)
    private Religion religion;
    @Column(name = "first_language") private String firstLanguage;
    @Column(name = "requires_interpreter") private Boolean requiresInterpreter;
    @Column(name = "english_difficulty") private Boolean englishDifficulty;
    @Column(name = "english_support_details") private String englishSupportDetails;
    @Column(name = "has_disability") private Boolean hasDisability;
    @Column(name = "has_long_term_conditions") private Boolean hasLongTermConditions;
    @Column(name = "has_armed_forces_affiliation") private Boolean hasArmedForcesAffiliation;
    @Column(name = "expecting_or_child_under_24m") private Boolean expectingOrChildUnder24m;

    @Column(name = "accepted_privacy_policy", nullable = false)
    private boolean acceptedPrivacyPolicy;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    // Opcionális verziókövetés későbbre:
    @Column(name = "supersedes_id", columnDefinition = "uuid")
   private UUID supersedesId;

}
