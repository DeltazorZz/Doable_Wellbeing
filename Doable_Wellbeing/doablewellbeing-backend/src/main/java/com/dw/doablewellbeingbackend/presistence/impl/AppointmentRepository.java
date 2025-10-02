package com.dw.doablewellbeingbackend.presistence.impl;

import com.dw.doablewellbeingbackend.presistence.entity.AppointmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface AppointmentRepository extends JpaRepository<AppointmentEntity, UUID> {
    @Query("""
        SELECT a FROM AppointmentEntity a
        WHERE a.coachId = :coachId
          AND a.status = 'scheduled'
          AND (a.startsAt < :endsAt AND a.endsAt > :startsAt)
    """)
    List<AppointmentEntity> findOverlaps(UUID coachId, OffsetDateTime startsAt, OffsetDateTime endsAt);
    List<AppointmentEntity> findByClientIdOrderByStartsAtDesc(UUID clientId);
    List<AppointmentEntity> findByCoachIdOrderByStartsAtDesc(UUID coachId);
}
