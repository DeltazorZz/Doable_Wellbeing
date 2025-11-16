package com.dw.backend.doablewellbeingbackend.presistence.impl;

import com.dw.backend.doablewellbeingbackend.presistence.entity.UserWidgetEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserWidgetRepository extends JpaRepository<UserWidgetEntity, UUID> {
    List<UserWidgetEntity> findByUserIdAndActiveIsTrueOrderByCreatedAtAsc(UUID userId);
}
