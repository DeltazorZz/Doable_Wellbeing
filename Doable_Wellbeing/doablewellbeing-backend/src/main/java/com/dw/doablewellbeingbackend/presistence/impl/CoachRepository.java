package com.dw.doablewellbeingbackend.presistence.impl;

import com.dw.doablewellbeingbackend.presistence.entity.CoachEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CoachRepository extends JpaRepository<CoachEntity, UUID> {
    boolean existsByUser(UUID userId);
}
