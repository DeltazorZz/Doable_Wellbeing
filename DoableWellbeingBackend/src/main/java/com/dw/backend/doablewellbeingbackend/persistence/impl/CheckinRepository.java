package com.dw.backend.doablewellbeingbackend.persistence.impl;


import com.dw.backend.doablewellbeingbackend.persistence.entity.CheckinEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CheckinRepository extends JpaRepository<CheckinEntity, UUID> {
    List<CheckinEntity> findTop10ByUserIdOrderByCreatedAtDesc(UUID userId);
}
