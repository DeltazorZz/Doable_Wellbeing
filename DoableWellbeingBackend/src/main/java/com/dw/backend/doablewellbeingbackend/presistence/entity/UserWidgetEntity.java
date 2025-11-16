package com.dw.backend.doablewellbeingbackend.presistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_widgets")
@Data @Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserWidgetEntity {

    @Id
    @GeneratedValue(generator = "UUID")
    @Column(columnDefinition = "uuid") private UUID id;

    @Column(name="user_id", nullable=false, columnDefinition="uuid")
    private UUID userId;

    @Column(nullable=false) private String type; // pl. HabitStreak
    private String title;

    @Column(nullable=false, columnDefinition="jsonb")
    private String settings = "{}"; // szerver oldalt String-ként tároljuk

    @Column(name="is_active", nullable=false) private boolean active = true;

    @Column(name="created_at", insertable=false, updatable=false)
    private OffsetDateTime createdAt;

    @Column(name="updated_at", insertable=false, updatable=false)
    private OffsetDateTime updatedAt;



}
