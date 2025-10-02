package com.dw.doablewellbeingbackend.presistence.impl;

import com.dw.doablewellbeingbackend.presistence.entity.ClientEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ClientRepository extends JpaRepository<ClientEntity, UUID> {
    boolean existsByUser(UUID userId);
}
