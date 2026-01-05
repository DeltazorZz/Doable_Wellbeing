package com.dw.backend.doablewellbeingbackend.persistence.impl;

import com.dw.backend.doablewellbeingbackend.persistence.entity.AppointmentNoteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface AppointmentNoteRepository extends JpaRepository<AppointmentNoteEntity, UUID> {
    List<AppointmentNoteEntity> findByAppointmentIdOrderByCreatedAtAsc(UUID appointmentId);
}
