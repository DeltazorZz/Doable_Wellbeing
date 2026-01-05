package com.dw.backend.doablewellbeingbackend.persistence.impl;

import com.dw.backend.doablewellbeingbackend.domain.dashboard.Breakpoint;
import com.dw.backend.doablewellbeingbackend.persistence.entity.DashboardWidgetPlacementEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DashboardWidgetPlacementRepository extends JpaRepository<DashboardWidgetPlacementEntity, UUID> {
    List<DashboardWidgetPlacementEntity> findByWidgetIdIn(Collection<UUID> widgetIds);
    Optional<DashboardWidgetPlacementEntity> findByWidgetIdAndBreakpoint(UUID widgetId, Breakpoint breakpoint);
}
