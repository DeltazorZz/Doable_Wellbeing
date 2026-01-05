package com.dw.backend.doablewellbeingbackend.persistence.entity;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "habit_logs",
        uniqueConstraints = @UniqueConstraint(columnNames = {"habit_id", "logged_at"})
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class HabitLogEntity {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(name = "habit_id", nullable = false)
    private UUID habitId;

    @Column(name = "logged_date", nullable = false)
    private LocalDate loggedDate;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;
}

