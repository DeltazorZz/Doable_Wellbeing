package com.dw.backend.doablewellbeingbackend.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(name = "micro_habit_catalog")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class MicroHabitCatalogEntity {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private String title;

    private String notes;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false)
    private String tags; // JSON array string

    @Column(name = "is_active", nullable = false)
    private boolean isActive;
}