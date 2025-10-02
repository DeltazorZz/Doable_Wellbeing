package com.dw.doablewellbeingbackend.presistence.impl;

import com.dw.doablewellbeingbackend.presistence.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {
    boolean existsByEmail(String email);
    Optional<UserEntity> findByEmail(String email);
    Optional<UserEntity> findByNhsNumber(String nhsNumber);
    Page<UserEntity> findAllByIsActiveTrue(Pageable pageable);

}
