package com.dw.backend.doablewellbeingbackend.persistence.impl;


import com.dw.backend.doablewellbeingbackend.persistence.entity.SelfReferralEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SelfReferralRepository extends JpaRepository<SelfReferralEntity, UUID> {
    Optional<SelfReferralEntity> findFirstByUserIdOrderByCreatedAtDesc(UUID userId);

}
