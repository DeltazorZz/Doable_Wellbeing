package com.dw.backend.doablewellbeingbackend.it.dashboard;

import com.dw.backend.doablewellbeingbackend.business.dashboard.DashboardService;
import com.dw.backend.doablewellbeingbackend.domain.dashboard.*;
import com.dw.backend.doablewellbeingbackend.it.IntegrationTestBase;
import com.dw.backend.doablewellbeingbackend.it.TestSeed;
import com.dw.backend.doablewellbeingbackend.persistence.entity.DashboardEntity;
import com.dw.backend.doablewellbeingbackend.persistence.entity.DashboardWidgetEntity;
import com.dw.backend.doablewellbeingbackend.persistence.entity.DashboardWidgetPlacementEntity;
import com.dw.backend.doablewellbeingbackend.persistence.impl.DashboardRepository;
import com.dw.backend.doablewellbeingbackend.persistence.impl.DashboardWidgetPlacementRepository;
import com.dw.backend.doablewellbeingbackend.persistence.impl.DashboardWidgetRepository;
import com.dw.backend.doablewellbeingbackend.persistence.impl.ModuleRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class DashboardServiceIT extends IntegrationTestBase {

    @Autowired DashboardService dashboardService;

    @Autowired DashboardRepository dashboardRepository;
    @Autowired DashboardWidgetRepository widgetRepository;
    @Autowired DashboardWidgetPlacementRepository placementRepository;
    @Autowired ModuleRepository moduleRepository;

    @Autowired ObjectMapper objectMapper;
    @Autowired JdbcTemplate jdbc;

    UUID userId;

    @BeforeEach
    void seed() {
        TestSeed.ensureRole(jdbc, "user");
        userId = TestSeed.insertUser(jdbc, "dash_it_user@test.com", "Dash", "User".getBytes(StandardCharsets.UTF_8), "John", "Doe");
        TestSeed.assignRole(jdbc, userId, "user");

        // legyen legalább 1 modul, különben addWidget fail
        TestSeed.ensureModule(jdbc, "upcoming_meetings", "Upcoming Meetings", "Your next coaching sessions");
        TestSeed.ensureModule(jdbc, "mood_chart", "Mood Chart", "Mood timeline and trends");

    }

    @Test
    void getOrCreateDefaultDashboard_createsWhenMissing_andReturnsEmptyWidgets() {
        var out = dashboardService.getOrCreateDefaultDashboard(userId);

        assertThat(out.dashboardId()).isNotNull();
        assertThat(out.isDefault()).isTrue();
        assertThat(out.name()).isEqualTo("Default Dashboard");
        assertThat(out.widgets()).isEmpty();

        DashboardEntity db = dashboardRepository.findByUserIdAndIsDefaultTrue(userId).orElseThrow();
        assertThat(db.getId()).isEqualTo(out.dashboardId());
    }

    @Test
    void getOrCreateDefaultDashboard_reusesExisting() {
        var d1 = dashboardService.getOrCreateDefaultDashboard(userId);
        var d2 = dashboardService.getOrCreateDefaultDashboard(userId);

        assertThat(d2.dashboardId()).isEqualTo(d1.dashboardId());
        assertThat(d2.widgets()).isEmpty();
    }

    @Test
    void addWidget_createsWidgetAndDefaultPlacement() {
        var dash = dashboardService.getOrCreateDefaultDashboard(userId);

        ObjectNode settings = objectMapper.createObjectNode();
        settings.put("showDaysAhead", 7);

        var req = new CreateDashboardWidgetRequest(
                "upcoming_meetings",
                "My upcoming",
                settings,
                null,   // breakpoint -> default lg
                null, null, null, null // x,y,w,h -> defaults
        );

        UUID widgetId = dashboardService.addWidget(userId, dash.dashboardId(), req);
        assertThat(widgetId).isNotNull();

        DashboardWidgetEntity w = widgetRepository.findById(widgetId).orElseThrow();
        assertThat(w.getDashboardId()).isEqualTo(dash.dashboardId());
        assertThat(w.isActive()).isTrue();
        assertThat(w.getSettings().path("showDaysAhead").asInt()).isEqualTo(7);

        List<DashboardWidgetPlacementEntity> placements =
                placementRepository.findByWidgetIdIn(List.of(widgetId));
        assertThat(placements).hasSize(1);

        var p = placements.get(0);
        assertThat(p.getBreakpoint()).isEqualTo(Breakpoint.lg);
        assertThat(p.getW()).isEqualTo(4);
        assertThat(p.getH()).isEqualTo(3);
        assertThat(p.getX()).isEqualTo(0);
        assertThat(p.getY()).isEqualTo(0);
    }

    @Test
    void upsertPlacements_updatesExistingPlacement() {
        var dash = dashboardService.getOrCreateDefaultDashboard(userId);

        var req = new CreateDashboardWidgetRequest(
                "upcoming_meetings",
                null,
                objectMapper.createObjectNode(),
                0,
                0, 0, 4, "3"
        );
        UUID widgetId = dashboardService.addWidget(userId, dash.dashboardId(), req);

        var upsert = new UpdatePlacementsRequest(List.of(
                new PlacementUpsertRequest(
                        widgetId,
                        "lg",
                        2, 5, 6, 4,
                        2, 2,
                        12, 12,
                        false
                )
        ));

        dashboardService.upsertPlacements(userId, dash.dashboardId(), upsert);

        var p = placementRepository.findByWidgetIdAndBreakpoint(widgetId, Breakpoint.lg).orElseThrow();
        assertThat(p.getX()).isEqualTo(2);
        assertThat(p.getY()).isEqualTo(5);
        assertThat(p.getW()).isEqualTo(6);
        assertThat(p.getH()).isEqualTo(4);
        assertThat(p.getMinW()).isEqualTo(2);
        assertThat(p.getMaxW()).isEqualTo(12);
        assertThat(p.getIsStatic()).isFalse();
    }

    @Test
    void updateWidgetSettings_persistsJson() {
        var dash = dashboardService.getOrCreateDefaultDashboard(userId);

        UUID widgetId = dashboardService.addWidget(
                userId,
                dash.dashboardId(),
                new CreateDashboardWidgetRequest(
                        "mood_chart",
                        null,
                        objectMapper.createObjectNode(),
                        null, null, null, null, null
                )
        );

        ObjectNode newSettings = objectMapper.createObjectNode();
        newSettings.put("rangeDays", 30);

        dashboardService.updateWidgetSettings(
                userId,
                dash.dashboardId(),
                widgetId,
                new UpdateWidgetSettingsRequest(newSettings)
        );

        DashboardWidgetEntity w = widgetRepository.findById(widgetId).orElseThrow();
        assertThat(w.getSettings().path("rangeDays").asInt()).isEqualTo(30);
    }

    @Test
    void deleteWidget_marksInactive_andDefaultDashboardDoesNotReturnIt() {
        var dash = dashboardService.getOrCreateDefaultDashboard(userId);

        UUID widgetId = dashboardService.addWidget(
                userId,
                dash.dashboardId(),
                new CreateDashboardWidgetRequest(
                        "upcoming_meetings",
                        null,
                        objectMapper.createObjectNode(),
                        null, null, null, null, null
                )
        );

        // előtte benne van
        var before = dashboardService.getOrCreateDefaultDashboard(userId);
        assertThat(before.widgets()).hasSize(1);

        dashboardService.deleteWidget(userId, dash.dashboardId(), widgetId);

        DashboardWidgetEntity w = widgetRepository.findById(widgetId).orElseThrow();
        assertThat(w.isActive()).isFalse();

        // utána már nem listázza az aktív widgetek közt
        var after = dashboardService.getOrCreateDefaultDashboard(userId);
        assertThat(after.widgets()).isEmpty();
    }
}
