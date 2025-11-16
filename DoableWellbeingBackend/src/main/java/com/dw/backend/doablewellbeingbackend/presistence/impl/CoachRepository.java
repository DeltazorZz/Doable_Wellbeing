package com.dw.backend.doablewellbeingbackend.presistence.impl;

import com.dw.backend.doablewellbeingbackend.presistence.entity.CoachEntity;
import com.dw.backend.doablewellbeingbackend.presistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CoachRepository extends JpaRepository<CoachEntity, UUID> {
    boolean existsByUser(UserEntity userId);
}
