package com.dw.backend.doablewellbeingbackend.persistence.entity;

import com.dw.backend.doablewellbeingbackend.domain.dashboard.Breakpoint;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Table(name = "dashboard_widget_placements",
        uniqueConstraints = @UniqueConstraint(
                name = "ux_widget_breakpoint",
                columnNames = {"widget_id", "breakpoint"}
        ),
        indexes = @Index(name = "ix_widget_placements_widget", columnList = "widget_id")
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class DashboardWidgetPlacementEntity {

    @Id
    @UuidGenerator
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "widget_id", nullable = false, columnDefinition = "uuid")
    private UUID widgetId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Breakpoint breakpoint;

    @Column(nullable = false)
    private int x;

    @Column(nullable = false)
    private int y;

    @Column(nullable = false)
    private int w;

    @Column(nullable = false)
    private int h;

    @Column(name = "min_w")
    private Integer minW;

    @Column(name = "min_h")
    private Integer minH;

    @Column(name = "max_w")
    private Integer maxW;

    @Column(name = "max_h")
    private Integer maxH;

    @Column(name = "is_static")
    private Boolean isStatic;
}
