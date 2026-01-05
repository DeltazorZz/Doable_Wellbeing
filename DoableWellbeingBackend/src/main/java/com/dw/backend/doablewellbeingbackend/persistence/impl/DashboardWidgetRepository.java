package com.dw.backend.doablewellbeingbackend.persistence.impl;

import com.dw.backend.doablewellbeingbackend.persistence.entity.DashboardWidgetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DashboardWidgetRepository extends JpaRepository<DashboardWidgetEntity, UUID> {

    @Query("""
        select w
        from DashboardWidgetEntity w
        join DashboardEntity d on d.id = w.dashboardId
        where w.id = :widgetId and d.userId = :userId
    """)
    Optional<DashboardWidgetEntity> findOwnedWidget(@Param("userId") UUID userId, @Param("widgetId") UUID widgetId);

    List<DashboardWidgetEntity> findByDashboardIdAndIsActiveTrue(UUID dashboardId);

    Optional<DashboardWidgetEntity> findByIdAndDashboardId(UUID id, UUID dashboardId);

    List<DashboardWidgetEntity> findByIdIn(Collection<UUID> ids);
}
