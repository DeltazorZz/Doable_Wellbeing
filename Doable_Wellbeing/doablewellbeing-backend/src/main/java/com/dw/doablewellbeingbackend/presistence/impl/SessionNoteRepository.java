package com.dw.doablewellbeingbackend.presistence.impl;

import com.dw.doablewellbeingbackend.presistence.entity.SessionNoteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SessionNoteRepository extends JpaRepository<SessionNoteEntity, UUID> {

    List<SessionNoteEntity> findByAppointmentIdOrderByCreatedAt(UUID appointmentId);

}
