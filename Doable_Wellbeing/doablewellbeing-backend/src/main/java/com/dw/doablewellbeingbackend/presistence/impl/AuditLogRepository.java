package com.dw.doablewellbeingbackend.presistence.impl;

import com.dw.doablewellbeingbackend.presistence.entity.AuditLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AuditLogRepository extends JpaRepository<AuditLogEntity, UUID> {
}
