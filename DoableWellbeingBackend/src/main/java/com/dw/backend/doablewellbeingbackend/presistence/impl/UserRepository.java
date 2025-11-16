package com.dw.backend.doablewellbeingbackend.presistence.impl;

import com.dw.backend.doablewellbeingbackend.presistence.entity.UserEntity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {
    //boolean existsByEmailIgnoreCase(String email);
    Optional<UserEntity> findByEmailIgnoreCase(String email);
    Optional<UserEntity> findByNhsNumber(String nhsNumber);
    Page<UserEntity> findAllByIsActiveTrue(Pageable pageable);
    Page<UserEntity> findAll(Pageable pageable);
    boolean existsByEmailIgnoreCase(String email);
}
