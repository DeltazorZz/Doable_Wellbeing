package com.dw.backend.doablewellbeingbackend.persistence.impl;

import com.dw.backend.doablewellbeingbackend.persistence.entity.MoodLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface MoodLogRepository extends JpaRepository<MoodLogEntity, UUID> {
    List<MoodLogEntity> findByUserIdAndLoggedAtAfterOrderByLoggedAtAsc(UUID userId, OffsetDateTime after);

    @Query("""
    select m from MoodLogEntity m
    where m.userId = :userId and m.loggedAt >= :after
    order by m.loggedAt asc
  """)
    List<MoodLogEntity> findRecent(UUID userId, OffsetDateTime after);
}