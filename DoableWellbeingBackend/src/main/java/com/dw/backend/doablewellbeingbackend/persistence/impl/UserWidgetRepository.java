package com.dw.backend.doablewellbeingbackend.persistence.impl;

import com.dw.backend.doablewellbeingbackend.persistence.entity.UserWidgetEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserWidgetRepository extends JpaRepository<UserWidgetEntity, UUID> {
    List<UserWidgetEntity> findByUserIdAndActiveIsTrueOrderByCreatedAtAsc(UUID userId);
}
