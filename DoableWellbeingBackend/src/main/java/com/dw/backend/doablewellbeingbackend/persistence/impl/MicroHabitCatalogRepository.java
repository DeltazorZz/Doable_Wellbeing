package com.dw.backend.doablewellbeingbackend.persistence.impl;

import com.dw.backend.doablewellbeingbackend.persistence.entity.MicroHabitCatalogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MicroHabitCatalogRepository extends JpaRepository<MicroHabitCatalogEntity, UUID> {
    List<MicroHabitCatalogEntity> findByCategoryAndIsActiveTrue(String category);
}