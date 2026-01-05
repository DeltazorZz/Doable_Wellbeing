package com.dw.backend.doablewellbeingbackend.persistence.impl;

import com.dw.backend.doablewellbeingbackend.persistence.entity.DashboardEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DashboardRepository extends JpaRepository<DashboardEntity, UUID> {
    Optional<DashboardEntity> findByUserIdAndIsDefaultTrue(UUID userId);
    Optional<DashboardEntity> findByIdAndUserId(UUID id, UUID userId);
}
