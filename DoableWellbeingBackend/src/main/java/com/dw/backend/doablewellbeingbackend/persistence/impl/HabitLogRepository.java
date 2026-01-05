package com.dw.backend.doablewellbeingbackend.persistence.impl;

import com.dw.backend.doablewellbeingbackend.persistence.entity.HabitLogEntity;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HabitLogRepository extends JpaRepository<HabitLogEntity, UUID> {
    Optional<HabitLogEntity> findByHabitIdAndLoggedDate(UUID habitId, LocalDate loggedDate);

    @Query("""
    select hl from HabitLogEntity hl
    where hl.habitId = :habitId
    order by hl.loggedDate desc
  """)
    List<HabitLogEntity> findRecentByHabit(UUID habitId, PageRequest pageable);
}