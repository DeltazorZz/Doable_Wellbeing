package com.dw.backend.doablewellbeingbackend.persistence.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "dashboard_widgets", indexes = {
        @Index(name = "ix_dashboard_widgets_dashboard", columnList = "dashboard_id"),
        @Index(name = "ix_dashboard_widgets_module", columnList = "module_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardWidgetEntity {

    @Id
    @UuidGenerator
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "dashboard_id", nullable = false, columnDefinition = "uuid")
    private UUID dashboardId;

    @Column(name = "module_id", nullable = false, columnDefinition = "uuid")
    private UUID moduleId;

    private String title;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private JsonNode settings;

    @Column(name = "settings_version", nullable = false)
    private int settingsVersion;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    // optional legacy mapping, keep it if DB has it
    @Column(name = "legacy_user_widget_id", columnDefinition = "uuid")
    private UUID legacyUserWidgetId;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}