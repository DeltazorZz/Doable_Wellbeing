package com.dw.backend.doablewellbeingbackend.presistence.impl;


import com.dw.backend.doablewellbeingbackend.presistence.entity.AppointmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface AppointmentRepository extends JpaRepository<AppointmentEntity, UUID> {
    @Query("""
  SELECT a FROM AppointmentEntity a
  WHERE a.coachId = :coachId
    AND a.status IN ('SCHEDULED','CONFIRMED')
    AND a.startsAt < :endsAt
    AND a.endsAt   > :startsAt
""")

    List<AppointmentEntity> findOverlaps(
            @Param("choachId") UUID coachId,
            @Param("startsAt") OffsetDateTime startsAt,
            @Param("endsAt") OffsetDateTime endsAt
    );

    List<AppointmentEntity> findAllByClientIdOrderByStartsAtDesc(UUID clientId);
    List<AppointmentEntity> findAllByCoachIdOrderByStartsAtDesc(UUID coachId);
}
