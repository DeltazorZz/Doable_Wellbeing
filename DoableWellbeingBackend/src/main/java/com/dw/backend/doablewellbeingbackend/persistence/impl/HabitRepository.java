package com.dw.backend.doablewellbeingbackend.persistence.impl;

import com.dw.backend.doablewellbeingbackend.persistence.entity.HabitEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface HabitRepository extends JpaRepository<HabitEntity, UUID> {
    List<HabitEntity> findByUserIdAndIsActiveTrueOrderByCreatedAtDesc(UUID userId);

    boolean existsByUserIdAndTitleIgnoreCase(UUID userId, String title);
}