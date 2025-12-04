package com.dw.backend.doablewellbeingbackend.persistence.impl;

import com.dw.backend.doablewellbeingbackend.persistence.entity.CoachEntity;
import com.dw.backend.doablewellbeingbackend.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CoachRepository extends JpaRepository<CoachEntity, UUID> {
    boolean existsByUser(UserEntity userId);
}
