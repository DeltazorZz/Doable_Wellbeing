package com.dw.doablewellbeingbackend.business.impl.selfReferralUseCaseImpl;

import com.dw.doablewellbeingbackend.business.selfReferralUseCases.CreateSelfReferralUseCase;
import com.dw.doablewellbeingbackend.common.exception.ConflictException;
import com.dw.doablewellbeingbackend.domain.selfreferral.CreateSelfReferralRequest;
import com.dw.doablewellbeingbackend.domain.selfreferral.CreateSelfReferralResponse;
import com.dw.doablewellbeingbackend.presistence.entity.SelfReferralEntity;
import com.dw.doablewellbeingbackend.presistence.impl.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateSelfReferralUseCaseImpl  implements CreateSelfReferralUseCase {

    private final SelfReferralRepository selfReferralRepository;
    private final UserRepository userRepository;
    private final UserAddressRepository addressRepository;
    private final UserContactRepository contactRepository;
    private final ConsentsRepository consentsRepository;
    private final ReferralValidator validator;

    @Override
    @Transactional
    public CreateSelfReferralResponse create(UUID userId, CreateSelfReferralRequest r) {

        var user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("Authenticated user not found!"));

        validator.validateRequest(r, user.getEmail());

        if (r.getNhsNumber() != null && !r.getNhsNumber().isBlank()) {
            var owner = userRepository.findByNhsNumber(r.getNhsNumber()).orElse(null);
            if (owner != null && !owner.getId().equals(userId)) {
                throw new ConflictException("NHS number already associated with another user!");
            }
        }
        var latest = selfReferralRepository.findFirstByUserIdOrderByCreatedAtDesc(userId).orElse(null);
        var entity = SelfReferralEntity.builder()
                .userId(userId)
                .title(r.getTitle())
                .firstName(r.getFirstName())
                .lastName(r.getLastName())
                .dateOfBirth(r.getDateOfBirth())
                .genderIdentity(r.getGenderIdentity())
                .isGenderSameAsAssignedAtBirth(r.getIsGenderSameAsAssignedAtBirth())
                .nhsNumber(r.getNhsNumber())
                .addressLine1(r.getAddressLine1())
                .addressLine2(r.getAddressLine2())
                .addressLine3(r.getAddressLine3())
                .townCity(r.getTownCity())
                .county(r.getCounty())
                .postcode(r.getPostcode())
                .mobilePhone(r.getMobilePhone())
                .homePhone(r.getHomePhone())
                .emailAddress(r.getEmailAddress())
                .consentMobileContact(r.getConsentMobileContact())
                .consentSmsReminders(r.getConsentSmsReminders())
                .consentMobileVoicemail(r.getConsentMobileVoicemail())
                .consentHomeVoicemail(r.getConsentHomeVoicemail())
                .consentEmailContact(r.getConsentEmailContact())
                .presentingProblem(r.getPresentingProblem())
                .heardAboutUs(r.getHeardAboutUs())
                .maritalStatus(r.getMaritalStatus())
                .accommodationType(r.getAccommodationType())
                .employmentStatus(r.getEmploymentStatus())
                .sexualOrientation(r.getSexualOrientation())
                .ethnicOrigin(r.getEthnicOrigin())
                .religion(r.getReligion())
                .firstLanguage(r.getFirstLanguage())
                .requiresInterpreter(r.getRequiresInterpreter())
                .englishDifficulty(r.getEnglishDifficulty())
                .englishSupportDetails(r.getEnglishSupportDetails())
                .hasDisability(r.getHasDisability())
                .hasLongTermConditions(r.getHasLongTermConditions())
                .hasArmedForcesAffiliation(r.getHasArmedForcesAffiliation())
                .expectingOrChildUnder24m(r.getExpectingOrChildUnder24m())
                .acceptedPrivacyPolicy(r.isAcceptedPrivacyPolicy())
                .supersedesId(latest == null ? null : latest.getId())
                .build();
        entity = selfReferralRepository.save(entity);

        //Normalized Data
        user.setTitle(r.getTitle());
        user.setFirstName(r.getFirstName());
        user.setLastName(r.getLastName());
        user.setDateOfBirth(r.getDateOfBirth());
        user.setGenderIdentity(r.getGenderIdentity());
        user.setIsGenderSameAsAssignedAtBirth(r.getIsGenderSameAsAssignedAtBirth());

        if (r.getNhsNumber() != null && !r.getNhsNumber().isBlank()) user.setNhsNumber(r.getNhsNumber());
        userRepository.save(user);

        //Primary address
        addressRepository.upsertPrimary(userId,
                r.getAddressLine1(), r.getAddressLine2(), r.getAddressLine3(),
                r.getTownCity(), r.getCounty(), r.getPostcode());

        //Contacts
        contactRepository.upsertPrimaryEmail(userId, r.getEmailAddress());
        contactRepository.upsertPrimaryMobile(userId, r.getMobilePhone());
        if (r.getHomePhone() != null && !r.getHomePhone().isBlank()) {
            contactRepository.upsertPrimaryHome(userId, r.getHomePhone());
        }
            //Consents
            consentsRepository.syncCommunicationConsents(userId, r);

            // 5) Notifications (belső + user e-mail) – később plug-in
            // TODO: notificationService.notifyNewReferral(userId, entity.getId());

            // 6) Audit
            //TODO: auditService.log(userId, "self_referral_submitted", "self_referrals", entity.getId(), minimalMeta);

            return CreateSelfReferralResponse.builder()
                    .referralId(entity.getId())
                    .userId(userId)
                    .build();

    }
}
