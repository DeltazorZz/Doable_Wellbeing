package com.dw.backend.doablewellbeingbackend.persistence.impl;

import com.dw.backend.doablewellbeingbackend.persistence.entity.UserModuleLayoutsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserModuleLayoutsRepository extends JpaRepository<UserModuleLayoutsEntity, UUID> {
    Optional<UserModuleLayoutsEntity> findByUserIdAndName(UUID userId, String name);
}
