package com.dw.backend.doablewellbeingbackend.persistence.impl;

import com.dw.backend.doablewellbeingbackend.persistence.entity.AppointmentResourceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AppointmentResourceRepository extends JpaRepository<AppointmentResourceEntity, UUID> {
    List<AppointmentResourceEntity> findByAppointmentIdOrderByCreatedAtDesc(UUID appointmentId);
}