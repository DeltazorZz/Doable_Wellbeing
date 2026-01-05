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
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DashboardServiceImplTest {

    @Mock DashboardRepository dashboardRepository;
    @Mock DashboardWidgetRepository widgetRepository;
    @Mock DashboardWidgetPlacementRepository placementRepository;
    @Mock ModuleRepository moduleRepository;

    private ObjectMapper objectMapper;
    private DashboardServiceImpl service;

    private UUID userId;
    private UUID dashboardId;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();
        service = new DashboardServiceImpl(
                dashboardRepository,
                widgetRepository,
                placementRepository,
                moduleRepository,
                objectMapper
        );

        userId = UUID.randomUUID();
        dashboardId = UUID.randomUUID();
    }

    // -------------------------------------------------------------------------
    // getOrCreateDefaultDashboard
    // -------------------------------------------------------------------------

    @Test
    void getOrCreateDefaultDashboard_existingDefault_returnsDashboardWithWidgets() {
        DashboardEntity dash = DashboardEntity.builder()
                .id(dashboardId)
                .userId(userId)
                .name("Default Dashboard")
                .isDefault(true)
                .build();

        when(dashboardRepository.findByUserIdAndIsDefaultTrue(userId)).thenReturn(Optional.of(dash));

        UUID moduleId = UUID.randomUUID();
        UUID widgetId = UUID.randomUUID();

        DashboardWidgetEntity widget = DashboardWidgetEntity.builder()
                .id(widgetId)
                .dashboardId(dashboardId)
                .moduleId(moduleId)
                .title("My Widget")
                .settings(objectMapper.createObjectNode().put("k", "v"))
                .isActive(true)
                .build();

        when(widgetRepository.findByDashboardIdAndIsActiveTrue(dashboardId)).thenReturn(List.of(widget));

        ModuleEntity module = ModuleEntity.builder()
                .id(moduleId)
                .code("upcoming_meetings")
                .build();

        when(moduleRepository.findByIdIn(Set.of(moduleId))).thenReturn(List.of(module));

        DashboardWidgetPlacementEntity placement = DashboardWidgetPlacementEntity.builder()
                .id(UUID.randomUUID())
                .widgetId(widgetId)
                .breakpoint(Breakpoint.lg)
                .x(1).y(2).w(4).h(3)
                .minW(2).minH(2).maxW(12).maxH(12)
                .isStatic(false)
                .build();

        when(placementRepository.findByWidgetIdIn(List.of(widgetId))).thenReturn(List.of(placement));

        DashboardView out = service.getOrCreateDefaultDashboard(userId);

        assertNotNull(out);
        assertEquals(dashboardId, out.dashboardId());
        assertEquals("Default Dashboard", out.name());
        assertTrue(out.isDefault());
        assertEquals(1, out.widgets().size());

        DashboardWidgetView wv = out.widgets().get(0);
        assertEquals(widgetId, wv.id());
        assertEquals("upcoming_meetings", wv.moduleCode());
        assertEquals("My Widget", wv.title());
        assertTrue(wv.isActive());
        assertNotNull(wv.placements());
        assertTrue(wv.placements().containsKey("lg"));

        PlacementView pv = wv.placements().get("lg");
        assertEquals(1, pv.x());
        assertEquals(2, pv.y());
        assertEquals(4, pv.w());
        assertEquals(3, pv.h());

        verify(dashboardRepository, never()).save(any());
    }

    @Test
    void getOrCreateDefaultDashboard_missingDefault_createsOne_andReturnsIt() {
        when(dashboardRepository.findByUserIdAndIsDefaultTrue(userId)).thenReturn(Optional.empty());

        DashboardEntity savedDash = DashboardEntity.builder()
                .id(dashboardId)
                .userId(userId)
                .name("Default Dashboard")
                .isDefault(true)
                .build();

        when(dashboardRepository.save(any())).thenReturn(savedDash);
        when(widgetRepository.findByDashboardIdAndIsActiveTrue(dashboardId)).thenReturn(List.of());

        DashboardView out = service.getOrCreateDefaultDashboard(userId);

        assertNotNull(out);
        assertEquals(dashboardId, out.dashboardId());
        assertEquals("Default Dashboard", out.name());
        assertTrue(out.isDefault());
        assertNotNull(out.widgets());
        assertTrue(out.widgets().isEmpty());

        verify(dashboardRepository).save(any(DashboardEntity.class));
    }

    @Test
    void getOrCreateDefaultDashboard_moduleMissing_fallsBackToUnknown() {
        DashboardEntity dash = DashboardEntity.builder()
                .id(dashboardId)
                .userId(userId)
                .name("Default Dashboard")
                .isDefault(true)
                .build();

        when(dashboardRepository.findByUserIdAndIsDefaultTrue(userId)).thenReturn(Optional.of(dash));

        UUID moduleId = UUID.randomUUID();
        UUID widgetId = UUID.randomUUID();

        DashboardWidgetEntity widget = DashboardWidgetEntity.builder()
                .id(widgetId)
                .dashboardId(dashboardId)
                .moduleId(moduleId)
                .title("W")
                .settings(objectMapper.createObjectNode())
                .isActive(true)
                .build();

        when(widgetRepository.findByDashboardIdAndIsActiveTrue(dashboardId)).thenReturn(List.of(widget));

        // moduleRepository doesn't return this moduleId => mapping missing
        when(moduleRepository.findByIdIn(Set.of(moduleId))).thenReturn(List.of());

        when(placementRepository.findByWidgetIdIn(List.of(widgetId))).thenReturn(List.of());

        DashboardView out = service.getOrCreateDefaultDashboard(userId);

        assertEquals(1, out.widgets().size());
        assertEquals("unknown", out.widgets().get(0).moduleCode());
    }

    // -------------------------------------------------------------------------
    // addWidget
    // -------------------------------------------------------------------------

    @Test
    void addWidget_dashboardNotFoundOrAccessDenied_throws() {
        UUID widgetDashId = UUID.randomUUID();
        CreateDashboardWidgetRequest req = new CreateDashboardWidgetRequest(
                "upcoming_meetings", "Title", null,
                null, null, null, null, null
        );

        when(dashboardRepository.findByIdAndUserId(widgetDashId, userId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                service.addWidget(userId, widgetDashId, req)
        );

        verifyNoInteractions(moduleRepository, widgetRepository, placementRepository);
    }

    @Test
    void addWidget_unknownModuleCode_throws() {
        DashboardEntity dash = DashboardEntity.builder().id(dashboardId).userId(userId).build();

        when(dashboardRepository.findByIdAndUserId(dashboardId, userId)).thenReturn(Optional.of(dash));
        when(moduleRepository.findByCode("nope")).thenReturn(Optional.empty());

        CreateDashboardWidgetRequest req = new CreateDashboardWidgetRequest(
                "nope", "Title", null,
                null, null, null, null, null
        );

        assertThrows(IllegalArgumentException.class, () ->
                service.addWidget(userId, dashboardId, req)
        );

        verifyNoInteractions(widgetRepository, placementRepository);
    }

    @Test
    void addWidget_settingsNull_usesEmptyObjectNode_andDefaultPlacement_andBreakpointLg() {
        DashboardEntity dash = DashboardEntity.builder().id(dashboardId).userId(userId).build();
        UUID moduleId = UUID.randomUUID();

        ModuleEntity module = ModuleEntity.builder().id(moduleId).code("upcoming_meetings").build();

        when(dashboardRepository.findByIdAndUserId(dashboardId, userId)).thenReturn(Optional.of(dash));
        when(moduleRepository.findByCode("upcoming_meetings")).thenReturn(Optional.of(module));

        // widget save returns widget with id
        UUID widgetId = UUID.randomUUID();
        when(widgetRepository.save(any())).thenAnswer(inv -> {
            DashboardWidgetEntity e = inv.getArgument(0);
            e.setId(widgetId); // if no setter, return a new builder copy with id
            return e;
        });

        when(placementRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CreateDashboardWidgetRequest req = new CreateDashboardWidgetRequest(
                "upcoming_meetings",
                null,
                null,          // settings null
                null,                   // should default lg
                null, null,         // w/h default
                null, null              // x/y default
        );

        UUID outId = service.addWidget(userId, dashboardId, req);
        assertEquals(widgetId, outId);

        ArgumentCaptor<DashboardWidgetEntity> widgetCaptor = ArgumentCaptor.forClass(DashboardWidgetEntity.class);
        verify(widgetRepository).save(widgetCaptor.capture());

        DashboardWidgetEntity savedWidget = widgetCaptor.getValue();
        assertEquals(dashboardId, savedWidget.getDashboardId());
        assertEquals(moduleId, savedWidget.getModuleId());
        assertNull(savedWidget.getTitle());
        assertTrue(savedWidget.isActive());
        assertNotNull(savedWidget.getSettings());
        assertTrue(savedWidget.getSettings().isObject());
        assertEquals(0, savedWidget.getSettings().size()); // empty object node

        ArgumentCaptor<DashboardWidgetPlacementEntity> placementCaptor = ArgumentCaptor.forClass(DashboardWidgetPlacementEntity.class);
        verify(placementRepository).save(placementCaptor.capture());

        DashboardWidgetPlacementEntity pl = placementCaptor.getValue();
        assertEquals(widgetId, pl.getWidgetId());
        assertEquals(Breakpoint.lg, pl.getBreakpoint());
        assertEquals(0, pl.getX());
        assertEquals(0, pl.getY());
        assertEquals(4, pl.getW());
        assertEquals(3, pl.getH());
    }

    @Test
    void addWidget_usesProvidedPlacementAndBreakpoint() {
        DashboardEntity dash = DashboardEntity.builder().id(dashboardId).userId(userId).build();
        UUID moduleId = UUID.randomUUID();
        ModuleEntity module = ModuleEntity.builder().id(moduleId).code("completed_meetings").build();

        when(dashboardRepository.findByIdAndUserId(dashboardId, userId)).thenReturn(Optional.of(dash));
        when(moduleRepository.findByCode("completed_meetings")).thenReturn(Optional.of(module));

        UUID widgetId = UUID.randomUUID();
        when(widgetRepository.save(any())).thenAnswer(inv -> {
            DashboardWidgetEntity e = inv.getArgument(0);
            e.setId(widgetId);
            return e;
        });

        when(placementRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ObjectNode settings = objectMapper.createObjectNode().put("maxItems", 5);

        CreateDashboardWidgetRequest req = new CreateDashboardWidgetRequest(
                "completed_meetings",
                "Done",
                settings,
                3,
                7,
                6,
                2,
                "md"
        );

        UUID outId = service.addWidget(userId, dashboardId, req);
        assertEquals(widgetId, outId);

        ArgumentCaptor<DashboardWidgetPlacementEntity> placementCaptor = ArgumentCaptor.forClass(DashboardWidgetPlacementEntity.class);
        verify(placementRepository).save(placementCaptor.capture());

        DashboardWidgetPlacementEntity pl = placementCaptor.getValue();
        assertEquals(Breakpoint.md, pl.getBreakpoint());
        assertEquals(3, pl.getX());
        assertEquals(7, pl.getY());
        assertEquals(6, pl.getW());
        assertEquals(2, pl.getH());
    }

    // -------------------------------------------------------------------------
    // upsertPlacements
    // -------------------------------------------------------------------------

    @Test
    void upsertPlacements_dashboardNotFoundOrAccessDenied_throws() {
        when(dashboardRepository.findByIdAndUserId(dashboardId, userId)).thenReturn(Optional.empty());

        UpdatePlacementsRequest req = new UpdatePlacementsRequest(List.of(
                new PlacementUpsertRequest(UUID.randomUUID(), "lg", 0, 0, 4, 3, null, null, null, null, false)
        ));

        assertThrows(IllegalArgumentException.class, () ->
                service.upsertPlacements(userId, dashboardId, req)
        );

        verifyNoInteractions(widgetRepository, placementRepository);
    }

    @Test
    void upsertPlacements_emptyPlacements_returnsEarly() {
        DashboardEntity dash = DashboardEntity.builder().id(dashboardId).userId(userId).build();
        when(dashboardRepository.findByIdAndUserId(dashboardId, userId)).thenReturn(Optional.of(dash));

        service.upsertPlacements(userId, dashboardId, new UpdatePlacementsRequest(List.of()));

        verifyNoInteractions(widgetRepository, placementRepository);
    }

    @Test
    void upsertPlacements_widgetNotPartOfDashboard_throws() {
        DashboardEntity dash = DashboardEntity.builder().id(dashboardId).userId(userId).build();
        when(dashboardRepository.findByIdAndUserId(dashboardId, userId)).thenReturn(Optional.of(dash));

        UUID widgetId = UUID.randomUUID();
        UpdatePlacementsRequest req = new UpdatePlacementsRequest(List.of(
                new PlacementUpsertRequest(widgetId, "lg", 0, 0, 4, 3, null, null, null, null, false)
        ));

        // widget exists but in another dashboard
        DashboardWidgetEntity w = DashboardWidgetEntity.builder()
                .id(widgetId)
                .dashboardId(UUID.randomUUID())
                .build();

        when(widgetRepository.findByIdIn(Set.of(widgetId))).thenReturn(List.of(w));

        assertThrows(IllegalArgumentException.class, () ->
                service.upsertPlacements(userId, dashboardId, req)
        );

        verify(placementRepository, never()).save(any());
    }

    @Test
    void upsertPlacements_existingPlacement_updatesAndSaves() {
        DashboardEntity dash = DashboardEntity.builder().id(dashboardId).userId(userId).build();
        when(dashboardRepository.findByIdAndUserId(dashboardId, userId)).thenReturn(Optional.of(dash));

        UUID widgetId = UUID.randomUUID();
        UpdatePlacementsRequest req = new UpdatePlacementsRequest(List.of(
                new PlacementUpsertRequest(widgetId, "lg", 1, 2, 6, 4, 2, 2, 12, 12, true)
        ));

        DashboardWidgetEntity w = DashboardWidgetEntity.builder().id(widgetId).dashboardId(dashboardId).build();
        when(widgetRepository.findByIdIn(Set.of(widgetId))).thenReturn(List.of(w));

        DashboardWidgetPlacementEntity existing = DashboardWidgetPlacementEntity.builder()
                .id(UUID.randomUUID())
                .widgetId(widgetId)
                .breakpoint(Breakpoint.lg)
                .x(0).y(0).w(4).h(3)
                .build();

        when(placementRepository.findByWidgetIdAndBreakpoint(widgetId, Breakpoint.lg))
                .thenReturn(Optional.of(existing));

        when(placementRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.upsertPlacements(userId, dashboardId, req);

        ArgumentCaptor<DashboardWidgetPlacementEntity> captor = ArgumentCaptor.forClass(DashboardWidgetPlacementEntity.class);
        verify(placementRepository).save(captor.capture());

        DashboardWidgetPlacementEntity saved = captor.getValue();
        assertEquals(1, saved.getX());
        assertEquals(2, saved.getY());
        assertEquals(6, saved.getW());
        assertEquals(4, saved.getH());
        assertEquals(2, saved.getMinW());
        assertEquals(2, saved.getMinH());
        assertEquals(12, saved.getMaxW());
        assertEquals(12, saved.getMaxH());
        assertEquals(true, saved.getIsStatic());
    }

    @Test
    void upsertPlacements_missingPlacement_createsNewAndSaves() {
        DashboardEntity dash = DashboardEntity.builder().id(dashboardId).userId(userId).build();
        when(dashboardRepository.findByIdAndUserId(dashboardId, userId)).thenReturn(Optional.of(dash));

        UUID widgetId = UUID.randomUUID();
        UpdatePlacementsRequest req = new UpdatePlacementsRequest(List.of(
                new PlacementUpsertRequest(widgetId, "md", 5, 6, 7, 8, null, null, null, null, false)
        ));

        DashboardWidgetEntity w = DashboardWidgetEntity.builder().id(widgetId).dashboardId(dashboardId).build();
        when(widgetRepository.findByIdIn(Set.of(widgetId))).thenReturn(List.of(w));

        when(placementRepository.findByWidgetIdAndBreakpoint(widgetId, Breakpoint.md))
                .thenReturn(Optional.empty());

        when(placementRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.upsertPlacements(userId, dashboardId, req);

        ArgumentCaptor<DashboardWidgetPlacementEntity> captor = ArgumentCaptor.forClass(DashboardWidgetPlacementEntity.class);
        verify(placementRepository).save(captor.capture());

        DashboardWidgetPlacementEntity saved = captor.getValue();
        assertEquals(widgetId, saved.getWidgetId());
        assertEquals(Breakpoint.md, saved.getBreakpoint());
        assertEquals(5, saved.getX());
        assertEquals(6, saved.getY());
        assertEquals(7, saved.getW());
        assertEquals(8, saved.getH());
    }

    // -------------------------------------------------------------------------
    // updateWidgetSettings
    // -------------------------------------------------------------------------

    @Test
    void updateWidgetSettings_dashboardNotFoundOrAccessDenied_throws() {
        when(dashboardRepository.findByIdAndUserId(dashboardId, userId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                service.updateWidgetSettings(userId, dashboardId, UUID.randomUUID(),
                        new UpdateWidgetSettingsRequest(objectMapper.createObjectNode()))
        );

        verifyNoInteractions(widgetRepository);
    }

    @Test
    void updateWidgetSettings_widgetNotFoundInDashboard_throws() {
        DashboardEntity dash = DashboardEntity.builder().id(dashboardId).userId(userId).build();
        when(dashboardRepository.findByIdAndUserId(dashboardId, userId)).thenReturn(Optional.of(dash));

        when(widgetRepository.findByIdAndDashboardId(any(), eq(dashboardId))).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                service.updateWidgetSettings(userId, dashboardId, UUID.randomUUID(),
                        new UpdateWidgetSettingsRequest(objectMapper.createObjectNode()))
        );

        verify(widgetRepository, never()).save(any());
    }

    @Test
    void updateWidgetSettings_success_updatesSettings_andFixesSettingsVersionIfInvalid() {
        DashboardEntity dash = DashboardEntity.builder().id(dashboardId).userId(userId).build();
        when(dashboardRepository.findByIdAndUserId(dashboardId, userId)).thenReturn(Optional.of(dash));

        UUID widgetId = UUID.randomUUID();
        DashboardWidgetEntity widget = DashboardWidgetEntity.builder()
                .id(widgetId)
                .dashboardId(dashboardId)
                .settings(objectMapper.createObjectNode().put("old", 1))
                .settingsVersion(0) // invalid -> should become 1
                .isActive(true)
                .build();

        when(widgetRepository.findByIdAndDashboardId(widgetId, dashboardId)).thenReturn(Optional.of(widget));
        when(widgetRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ObjectNode newSettings = objectMapper.createObjectNode().put("maxItems", 5);

        service.updateWidgetSettings(userId, dashboardId, widgetId, new UpdateWidgetSettingsRequest(newSettings));

        assertEquals(newSettings, widget.getSettings());
        assertEquals(1, widget.getSettingsVersion());

        verify(widgetRepository).save(widget);
    }

    // -------------------------------------------------------------------------
    // deleteWidget
    // -------------------------------------------------------------------------

    @Test
    void deleteWidget_dashboardNotFoundOrAccessDenied_throws() {
        when(dashboardRepository.findByIdAndUserId(dashboardId, userId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                service.deleteWidget(userId, dashboardId, UUID.randomUUID())
        );

        verifyNoInteractions(widgetRepository);
    }

    @Test
    void deleteWidget_widgetNotFoundInDashboard_throws() {
        DashboardEntity dash = DashboardEntity.builder().id(dashboardId).userId(userId).build();
        when(dashboardRepository.findByIdAndUserId(dashboardId, userId)).thenReturn(Optional.of(dash));

        when(widgetRepository.findByIdAndDashboardId(any(), eq(dashboardId))).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                service.deleteWidget(userId, dashboardId, UUID.randomUUID())
        );

        verify(widgetRepository, never()).save(any());
    }

    @Test
    void deleteWidget_success_setsInactiveAndSaves() {
        DashboardEntity dash = DashboardEntity.builder().id(dashboardId).userId(userId).build();
        when(dashboardRepository.findByIdAndUserId(dashboardId, userId)).thenReturn(Optional.of(dash));

        UUID widgetId = UUID.randomUUID();
        DashboardWidgetEntity widget = DashboardWidgetEntity.builder()
                .id(widgetId)
                .dashboardId(dashboardId)
                .isActive(true)
                .build();

        when(widgetRepository.findByIdAndDashboardId(widgetId, dashboardId)).thenReturn(Optional.of(widget));
        when(widgetRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.deleteWidget(userId, dashboardId, widgetId);

        assertFalse(widget.isActive());
        verify(widgetRepository).save(widget);
    }
}
