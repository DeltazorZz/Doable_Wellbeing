package com.dw.backend.doablewellbeingbackend.persistence.impl;

import com.dw.backend.doablewellbeingbackend.persistence.entity.CoachAvailability;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public interface CoachAvailabilityRepository  extends JpaRepository<CoachAvailability, UUID> {

    List<CoachAvailability> findByCoachIdAndDateBetweenOrderByDateAscStartTimeAsc(
            UUID coachId,
            LocalDate from,
            LocalDate to
    );
    List<CoachAvailability> findBySeriesId(UUID seriesId);

    List<CoachAvailability> findByCoachIdAndDateBetweenAndIsActiveTrue(UUID coachId, LocalDate dateAfter, LocalDate dateBefore);
}
