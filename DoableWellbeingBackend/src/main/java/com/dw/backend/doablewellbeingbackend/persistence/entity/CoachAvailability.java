package com.dw.backend.doablewellbeingbackend.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;


@Entity
@Table(name = "coach_availabilities", uniqueConstraints = {@UniqueConstraint(name = "ux_coach_availability_slot", columnNames =  {"coach_id", "date", "start_time", "end_time"})})
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CoachAvailability {

    @Id
    @UuidGenerator
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "coach_id", nullable = false)
    @JdbcTypeCode(SqlTypes.UUID)
    private UUID coachId;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;


    @Column(name = "is_recurring", nullable = false)
    private boolean isRecurring;


    @Column(name = "series_id")
    @JdbcTypeCode(SqlTypes.UUID)
    private UUID seriesId;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

}
