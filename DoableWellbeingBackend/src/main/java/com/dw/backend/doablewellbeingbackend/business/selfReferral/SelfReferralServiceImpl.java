package com.dw.backend.doablewellbeingbackend.business.selfReferral;


import com.dw.backend.doablewellbeingbackend.common.exception.ConflictException;
import com.dw.backend.doablewellbeingbackend.common.exception.NotFoundException;
import com.dw.backend.doablewellbeingbackend.domain.selfreferral.CreateSelfReferralRequest;
import com.dw.backend.doablewellbeingbackend.domain.selfreferral.CreateSelfReferralResponse;
import com.dw.backend.doablewellbeingbackend.domain.selfreferral.SelfReferralStatusResponse;
import com.dw.backend.doablewellbeingbackend.domain.selfreferral.SelfReferralView;
import com.dw.backend.doablewellbeingbackend.presistence.entity.SelfReferralEntity;
import com.dw.backend.doablewellbeingbackend.presistence.impl.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.Period;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SelfReferralServiceImpl implements SelfReferralService {

   // private final NotificationService notificationService;
    //private final AuditService auditService;
    private final SelfReferralRepository selfRepo;
    private final UserRepository userRepo;
    private final ReferralValidator validator;
    private final UserAddressRepository addressRepository;
    //private final UserContactRepository contactRepository;
    //private final ConsentsRepository consentsRepository;
    private static final int MONTHS_VALID = 6;


    //Create SelfReferral
    @Override
    @Transactional
    public CreateSelfReferralResponse create(UUID userId, CreateSelfReferralRequest r) {

        var user = userRepo.findById(userId).orElseThrow(() -> new IllegalArgumentException("Authenticated user not found!"));

        validator.validateRequest(r, user.getEmail());

        if (r.getNhsNumber() != null && !r.getNhsNumber().isBlank()) {
            var owner = userRepo.findByNhsNumber(r.getNhsNumber()).orElse(null);
            if (owner != null && !owner.getId().equals(userId)) {
                throw new ConflictException("NHS number already associated with another user!");
            }
        }
        var latest = selfRepo.findFirstByUserIdOrderByCreatedAtDesc(userId).orElse(null);
        var entity = SelfReferralEntity.builder()
                .userId(userId)
                .title(r.getTitle())
                .genderIdentity(r.getGenderIdentity())
                .isGenderSameAsAssignedAtBirth(r.getIsGenderSameAsAssignedAtBirth())
                .mobilePhone(r.getMobilePhone())
                .homePhone(r.getHomePhone())
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
                .supersedesId(latest == null ? null : latest.getId())
                .build();
        entity = selfRepo.save(entity);

        user.setFirstName(r.getFirstName());
        user.setLastName(r.getLastName());
        user.setDateOfBirth(r.getDateOfBirth());

        if (r.getNhsNumber() != null && !r.getNhsNumber().isBlank()) user.setNhsNumber(r.getNhsNumber());
        userRepo.save(user);

        //Primary address
        addressRepository.upsertPrimary(userId,
                r.getAddressLine1(), r.getAddressLine2(), r.getAddressLine3(),
                r.getTownCity(), r.getCounty(), r.getPostcode());

        //Contacts
//        contactRepository.upsertPrimaryEmail(userId, r.getEmailAddress());
//        contactRepository.upsertPrimaryMobile(userId, r.getMobilePhone());
//        if (r.getHomePhone() != null && !r.getHomePhone().isBlank()) {
//            contactRepository.upsertPrimaryHome(userId, r.getHomePhone());
//        }
//        //Consents
//        consentsRepository.syncCommunicationConsents(userId, r);

        // 5) Notifications (belső + user e-mail) – később plug-in
       // notificationService.notifyNewReferral(userId, entity.getId());

        // 6) Audit
        //auditService.log(userId, "self_referral_submitted", "self_referrals", entity.getId(), minimalMeta);

        return CreateSelfReferralResponse.builder()
                .referralId(entity.getId())
                .userId(userId)
                .build();

    }


    //Get latest selfref implementation
    @Override
    public SelfReferralView getLatest(UUID userId){
        var latest = selfRepo.findFirstByUserIdOrderByCreatedAtDesc(userId).orElseThrow(() -> new NotFoundException("No SelfReferral found"));
        return SelfreferralMapper.toView(latest);
    }


    //Get selfref status implementation
    @Override
    public SelfReferralStatusResponse getStatus(UUID userId){
        var latest = selfRepo.findFirstByUserIdOrderByCreatedAtDesc(userId).orElse(null);

        var months = Period.between(latest.getCreatedAt().toLocalDate(), OffsetDateTime.now().toLocalDate()).getMonths();
        boolean fresh = months < MONTHS_VALID;
        return SelfReferralStatusResponse.builder().hasValidReferral(true).lastReferralId(latest.getId()).lastUpdatedAt(latest.getCreatedAt()).needsReview(!fresh).build();
    }






}
