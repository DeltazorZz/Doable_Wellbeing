package com.dw.backend.doablewellbeingbackend.presistence.entity;

import com.dw.backend.doablewellbeingbackend.domain.enums.*;
import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.OffsetDateTime;
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


    @Column(name = "mobile_phone") private String mobilePhone;
    @Column(name = "home_phone")   private String homePhone;


    @Column(name = "presenting_problem", columnDefinition = "text")
    private String presentingProblem;
    @Column(name = "heard_about_us")
    private String heardAboutUs;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private Title title;
    @Enumerated(EnumType.STRING)
    @Column(name = "gender_identity")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private GenderIdentity genderIdentity;
    @Column(name = "is_gender_same_as_assigned_at_birth")
    private Boolean isGenderSameAsAssignedAtBirth;
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "marital_status") private MaritalStatus maritalStatus;
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "accommodation_type") private AccommodationType accommodationType;
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "employment_status") private EmploymentStatus employmentStatus;
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "sexual_orientation") private SexualOrientation sexualOrientation;
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "ethnic_origin") private EthnicOrigin ethnicOrigin;
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private Religion religion;
    @Column(name = "first_language") private String firstLanguage;
    @Column(name = "requires_interpreter") private Boolean requiresInterpreter;
    @Column(name = "english_difficulty") private Boolean englishDifficulty;
    @Column(name = "english_support_details") private String englishSupportDetails;
    @Column(name = "has_disability") private Boolean hasDisability;
    @Column(name = "has_long_term_conditions") private Boolean hasLongTermConditions;
    @Column(name = "has_armed_forces_affiliation") private Boolean hasArmedForcesAffiliation;
    @Column(name = "expecting_or_child_under_24m") private Boolean expectingOrChildUnder24m;


    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();


    @Column(name = "supersedes_id", columnDefinition = "uuid")
    private UUID supersedesId;

}
