package com.dw.backend.doablewellbeingbackend.presistence.impl;


import com.dw.backend.doablewellbeingbackend.presistence.entity.SelfReferralEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SelfReferralRepository extends JpaRepository<SelfReferralEntity, UUID> {
    Optional<SelfReferralEntity> findFirstByUserIdOrderByCreatedAtDesc(UUID userId);

}
