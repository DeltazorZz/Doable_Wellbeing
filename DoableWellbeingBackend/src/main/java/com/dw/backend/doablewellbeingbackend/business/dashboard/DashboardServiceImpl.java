package com.dw.backend.doablewellbeingbackend.business.dashboard;

import com.dw.backend.doablewellbeingbackend.domain.dashboard.*;
import com.dw.backend.doablewellbeingbackend.persistence.entity.DashboardEntity;
import com.dw.backend.doablewellbeingbackend.persistence.entity.DashboardWidgetEntity;
import com.dw.backend.doablewellbeingbackend.persistence.entity.DashboardWidgetPlacementEntity;
import com.dw.backend.doablewellbeingbackend.persistence.entity.ModuleEntity;
import com.dw.backend.doablewellbeingbackend.persistence.impl.DashboardRepository;
import com.dw.backend.doablewellbeingbackend.persistence.impl.DashboardWidgetPlacementRepository;
import com.dw.backend.doablewellbeingbackend.persistence.impl.DashboardWidgetRepository;
import com.dw.backend.doablewellbeingbackend.persistence.impl.ModuleRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final DashboardRepository dashboardRepository;
    private final DashboardWidgetRepository widgetRepository;
    private final DashboardWidgetPlacementRepository placementRepository;
    private final ModuleRepository moduleRepository;
    private final ObjectMapper objectMapper;


    @Override
    @Transactional
    public DashboardView getOrCreateDefaultDashboard(UUID userId) {
        DashboardEntity dashboardEntity = dashboardRepository.findByUserIdAndIsDefaultTrue(userId)
                .orElseGet(() -> createDefaultDashboard(userId));

        List<DashboardWidgetEntity> widgets = widgetRepository.findByDashboardIdAndIsActiveTrue(dashboardEntity.getId());

        Map<UUID, String> moduleIdToCode = resolveModuleCodes(widgets);

        Map<UUID, List<DashboardWidgetPlacementEntity>> placementsByWidgetId  = resolvePlacements(widgets);

        List<DashboardWidgetView> widgetViews = widgets.stream()
                .map(w -> toWidgetView(w, moduleIdToCode.get(w.getModuleId()), placementsByWidgetId.getOrDefault(w.getId(), List.of())))
                .toList();
        return new DashboardView(dashboardEntity.getId(), dashboardEntity.getName(), dashboardEntity.isDefault(), widgetViews);

    }

    @Override
    @Transactional
    public UUID addWidget(UUID userId, UUID dashboardId, CreateDashboardWidgetRequest request) {
        DashboardEntity dashboard = dashboardRepository.findByIdAndUserId(dashboardId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Dashboard not found or access denied"));

        ModuleEntity module = moduleRepository.findByCode(request.moduleCode())
                .orElseThrow(() -> new IllegalArgumentException("Unknown moduleCode: " + request.moduleCode()));

        OffsetDateTime now = OffsetDateTime.now();

        JsonNode settings = request.settings() != null ? request.settings() : objectMapper.createObjectNode();

        DashboardWidgetEntity widget = DashboardWidgetEntity.builder()
                .dashboardId(dashboard.getId())
                .moduleId(module.getId())
                .title(request.title())
                .settings(settings)
                .settingsVersion(1)
                .isActive(true)
                .createdAt(now)
                .updatedAt(now)
                .build();

        widget = widgetRepository.save(widget);


        Breakpoint bp = parseBreakpointOrDefault(request.breakpoint());

        int w = request.w() != null ? request.w() : 4;
        int h = request.h() != null ? request.h() : 3;
        int x = request.x() != null ? request.x() : 0;
        int y = request.y() != null ? request.y() : 0;

        DashboardWidgetPlacementEntity placement = DashboardWidgetPlacementEntity.builder()
                .widgetId(widget.getId())
                .breakpoint(bp)
                .x(x)
                .y(y)
                .w(w)
                .h(h)
                .build();

        placementRepository.save(placement);

        return widget.getId();
    }

    private Breakpoint parseBreakpointOrDefault(String bp) {
        if (bp == null || bp.isBlank()) return Breakpoint.lg;
        try {
            return Breakpoint.valueOf(bp);
        } catch (IllegalArgumentException ex) {
            return Breakpoint.lg;
        }
    }

    @Override
    @Transactional
    public void upsertPlacements(UUID userId, UUID dashboardId, UpdatePlacementsRequest request) {

        DashboardEntity dashboard = dashboardRepository.findByIdAndUserId(dashboardId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Dashboard not found or access denied"));


        Set<UUID> widgetIds = request.placements().stream()
                .map(PlacementUpsertRequest::widgetId)
                .collect(Collectors.toSet());

        if (widgetIds.isEmpty()) return;


        List<DashboardWidgetEntity> widgets = widgetRepository.findByIdIn(widgetIds);
        Set<UUID> allowedWidgetIds = widgets.stream()
                .filter(w -> w.getDashboardId().equals(dashboard.getId()))
                .map(DashboardWidgetEntity::getId)
                .collect(Collectors.toSet());


        if (allowedWidgetIds.size() != widgetIds.size()) {
            throw new IllegalArgumentException("One or more widgetIds are not part of this dashboard");
        }


        for (PlacementUpsertRequest p : request.placements()) {
            Breakpoint bp = parseBreakpointOrDefault(p.breakpoint());

            DashboardWidgetPlacementEntity entity = placementRepository
                    .findByWidgetIdAndBreakpoint(p.widgetId(), bp)
                    .orElseGet(() -> DashboardWidgetPlacementEntity.builder()
                            .widgetId(p.widgetId())
                            .breakpoint(bp)
                            .build()
                    );

            entity.setX(p.x());
            entity.setY(p.y());
            entity.setW(p.w());
            entity.setH(p.h());
            entity.setMinW(p.minW());
            entity.setMinH(p.minH());
            entity.setMaxW(p.maxW());
            entity.setMaxH(p.maxH());
            entity.setIsStatic(p.isStatic());

            placementRepository.save(entity);
        }
    }


    @Override
    @Transactional
    public void updateWidgetSettings(UUID userId, UUID dashboardId, UUID widgetId, UpdateWidgetSettingsRequest request) {

        DashboardEntity dashboard = dashboardRepository.findByIdAndUserId(dashboardId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Dashboard not found or access denied"));


        DashboardWidgetEntity widget = widgetRepository.findByIdAndDashboardId(widgetId, dashboard.getId())
                .orElseThrow(() -> new IllegalArgumentException("Widget not found in this dashboard"));

        widget.setSettings(request.settings());
        widget.setSettingsVersion(widget.getSettingsVersion() <= 0 ? 1 : widget.getSettingsVersion());
        widgetRepository.save(widget);
    }

    @Override
    @Transactional
    public void deleteWidget(UUID userId, UUID dashboardId, UUID widgetId) {
        DashboardEntity dashboard = dashboardRepository.findByIdAndUserId(dashboardId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Dashboard not found or access denied"));

        DashboardWidgetEntity widget = widgetRepository.findByIdAndDashboardId(widgetId, dashboard.getId())
                .orElseThrow(() -> new IllegalArgumentException("Widget not found in this dashboard"));

        widget.setActive(false);
        widgetRepository.save(widget);

    }

    @Transactional
    protected DashboardEntity createDefaultDashboard(UUID userId) {
        OffsetDateTime now = OffsetDateTime.now();
        DashboardEntity dashboardEntity = DashboardEntity.builder()
                .userId(userId)
                .name("Default Dashboard")
                .isDefault(true)
                .createdAt(now)
                .updatedAt(now)
                .build();
        return dashboardRepository.save(dashboardEntity);
    }

    private Map<UUID, String> resolveModuleCodes(List<DashboardWidgetEntity> widgets) {
        Set<UUID> moduleIds = widgets.stream().map(DashboardWidgetEntity::getModuleId).collect(Collectors.toSet());
        if (moduleIds.isEmpty()) return Map.of();

        return moduleRepository.findByIdIn(moduleIds).stream()
                .collect(Collectors.toMap(ModuleEntity::getId, ModuleEntity::getCode));
    }


    private Map<UUID, List<DashboardWidgetPlacementEntity>> resolvePlacements(List<DashboardWidgetEntity> widgets) {
        List<UUID>  widgetIds = widgets.stream().map(DashboardWidgetEntity::getId).collect(Collectors.toList());
        if (widgetIds.isEmpty()) return Map.of();

        return placementRepository.findByWidgetIdIn(widgetIds).stream()
                .collect(Collectors.groupingBy(DashboardWidgetPlacementEntity::getWidgetId));
    }

    private DashboardWidgetView toWidgetView(
            DashboardWidgetEntity w,
            String moduleCode,
            List<DashboardWidgetPlacementEntity> placements
    ) {
        Map<String, PlacementView> placementMap = placements.stream()
                .collect(Collectors.toMap(
                        p -> p.getBreakpoint().name(),
                        p -> new PlacementView(p.getX(), p.getY(), p.getW(), p.getH(), p.getMinW(),
                                p.getMinH(), p.getMaxW(), p.getMaxH(), p.getIsStatic())
                ));
        return new DashboardWidgetView(
                w.getId(),
                moduleCode != null ? moduleCode : "unknown",
                w.getTitle(),
                w.getSettings(),
                w.isActive(),
                placementMap
        );

    }


}
