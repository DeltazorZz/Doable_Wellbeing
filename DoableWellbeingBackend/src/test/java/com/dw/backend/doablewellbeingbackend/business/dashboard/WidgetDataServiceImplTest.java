package com.dw.backend.doablewellbeingbackend.business.dashboard;

import com.dw.backend.doablewellbeingbackend.domain.dashboard.*;
import com.dw.backend.doablewellbeingbackend.domain.enums.AppointmentStatus;
import com.dw.backend.doablewellbeingbackend.persistence.entity.*;
import com.dw.backend.doablewellbeingbackend.persistence.impl.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WidgetDataServiceImplTest {

    @Mock HabitRepository habitRepository;
    @Mock HabitLogRepository habitLogRepository;
    @Mock MoodLogRepository moodLogRepository;
    @Mock AppointmentRepository appointmentRepository;
    @Mock DashboardWidgetRepository widgetRepository;
    @Mock ModuleRepository moduleRepository;
    @Mock AppointmentNoteRepository appointmentNoteRepository;
    @Mock AppointmentResourceRepository appointmentResourceRepository;

    private WidgetDataServiceImpl service;
    private ObjectMapper objectMapper;

    private UUID userId;
    private UUID widgetId;
    private UUID moduleId;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        service = new WidgetDataServiceImpl(
                habitRepository,
                habitLogRepository,
                moodLogRepository,
                appointmentRepository,
                widgetRepository,
                moduleRepository,
                appointmentNoteRepository,
                appointmentResourceRepository
        );
        objectMapper = new ObjectMapper();

        userId = UUID.randomUUID();
        widgetId = UUID.randomUUID();
        moduleId = UUID.randomUUID();
    }

    // -------------------------------------------------------------------------
    // getWidgetData - guard clauses
    // -------------------------------------------------------------------------

    @Test
    void getWidgetData_widgetNotFound_throws() {
        when(widgetRepository.findOwnedWidget(userId, widgetId))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                service.getWidgetData(userId, widgetId)
        );
    }

    @Test
    void getWidgetData_moduleMissing_throws() {
        DashboardWidgetEntity widget = baseWidget("any");
        when(widgetRepository.findOwnedWidget(userId, widgetId))
                .thenReturn(Optional.of(widget));
        when(moduleRepository.findById(moduleId))
                .thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () ->
                service.getWidgetData(userId, widgetId)
        );
    }

    @Test
    void getWidgetData_unknownModule_returnsUnknownWidgetData() {
        DashboardWidgetEntity widget = baseWidget("unknown_code");
        when(widgetRepository.findOwnedWidget(userId, widgetId))
                .thenReturn(Optional.of(widget));
        when(moduleRepository.findById(moduleId))
                .thenReturn(Optional.of(ModuleEntity.builder()
                        .id(moduleId)
                        .code("unknown_code")
                        .build()));

        Object out = service.getWidgetData(userId, widgetId);

        assertTrue(out instanceof UnknownWidgetData);
        UnknownWidgetData uw = (UnknownWidgetData) out;
        assertEquals("unknown_code", uw.moduleCode());
    }

    // -------------------------------------------------------------------------
    // upcoming_meetings
    // -------------------------------------------------------------------------

    @Test
    void getWidgetData_upcomingMeetings_returnsMappedData() {
        ObjectNode settings = objectMapper.createObjectNode().put("showDaysAhead", 7);
        DashboardWidgetEntity widget = baseWidget("upcoming_meetings", settings);

        when(widgetRepository.findOwnedWidget(userId, widgetId))
                .thenReturn(Optional.of(widget));
        when(moduleRepository.findById(moduleId))
                .thenReturn(Optional.of(module("upcoming_meetings")));

        AppointmentEntity appt = AppointmentEntity.builder()
                .id(UUID.randomUUID())
                .startsAt(OffsetDateTime.now().plusDays(1))
                .endsAt(OffsetDateTime.now().plusDays(1).plusHours(1))
                .status(AppointmentStatus.scheduled)
                .meetingUrl("https://meet")
                .build();

        when(appointmentRepository.findUpcomingForClient(eq(userId), any(), any()))
                .thenReturn(List.of(appt));

        Object out = service.getWidgetData(userId, widgetId);

        assertTrue(out instanceof UpcomingMeetingsWidgetData);
        UpcomingMeetingsWidgetData data = (UpcomingMeetingsWidgetData) out;

        assertEquals(7, data.showDaysAhead());
        assertEquals(1, data.meetings().size());

        UpcomingMeetingsWidgetData.MeetingItem mi = data.meetings().get(0);
        assertEquals("https://meet", mi.meetingUrl());
        assertEquals("scheduled", mi.status());
        assertNotNull(mi.id());
        assertNotNull(mi.startsAt());
        assertNotNull(mi.endsAt());

    }

    // -------------------------------------------------------------------------
    // completed_meetings
    // -------------------------------------------------------------------------

    @Test
    void getWidgetData_completedMeetings_mapsNotesAndResources() {
        ObjectNode settings = objectMapper.createObjectNode().put("maxItems", 3);
        DashboardWidgetEntity widget = baseWidget("completed_meetings", settings);

        when(widgetRepository.findOwnedWidget(userId, widgetId))
                .thenReturn(Optional.of(widget));
        when(moduleRepository.findById(moduleId))
                .thenReturn(Optional.of(module("completed_meetings")));

        AppointmentEntity appt = AppointmentEntity.builder()
                .id(UUID.randomUUID())
                .startsAt(OffsetDateTime.now().minusDays(1))
                .build();

        when(appointmentRepository.findCompletedForClient(eq(userId), any(PageRequest.class)))
                .thenReturn(List.of(appt));

        when(appointmentNoteRepository.findByAppointmentIdOrderByCreatedAtAsc(appt.getId()))
                .thenReturn(List.of(
                        AppointmentNoteEntity.builder().note("Note 1").build(),
                        AppointmentNoteEntity.builder().note("Note 2").build()
                ));

        when(appointmentResourceRepository.findByAppointmentIdOrderByCreatedAtDesc(appt.getId()))
                .thenReturn(List.of(
                        AppointmentResourceEntity.builder()
                                .id(UUID.randomUUID())
                                .fileName("file.pdf")
                                .sizeBytes(2048L)
                                .url("http://file")
                                .build()
                ));

        Object out = service.getWidgetData(userId, widgetId);

        assertTrue(out instanceof CompletedMeetingsWidgetData);
        CompletedMeetingsWidgetData data = (CompletedMeetingsWidgetData) out;

        assertEquals(1, data.sessions().size());
        assertNotNull(data.sessions().get(0).coachSummary());
        assertEquals(1, data.sessions().get(0).files().size());
        assertEquals("2 KB", data.sessions().get(0).files().get(0).sizeLabel());
    }

    // -------------------------------------------------------------------------
    // mood_chart
    // -------------------------------------------------------------------------

    @Test
    void getWidgetData_moodChart_returnsPoints() {
        ObjectNode settings = objectMapper.createObjectNode().put("rangeDays", 5);
        DashboardWidgetEntity widget = baseWidget("mood_chart", settings);

        when(widgetRepository.findOwnedWidget(userId, widgetId))
                .thenReturn(Optional.of(widget));
        when(moduleRepository.findById(moduleId))
                .thenReturn(Optional.of(module("mood_chart")));

        when(moodLogRepository.findRecent(eq(userId), any()))
                .thenReturn(List.of(
                        MoodLogEntity.builder()
                                .loggedAt(OffsetDateTime.now())
                                .moodScore(7)
                                .build()
                ));

        Object out = service.getWidgetData(userId, widgetId);

        assertTrue(out instanceof MoodChartWidgetData);
        MoodChartWidgetData data = (MoodChartWidgetData) out;

        assertEquals(1, data.points().size());
        assertEquals(7, data.points().get(0).score());
    }

    // -------------------------------------------------------------------------
    // habit_tracker
    // -------------------------------------------------------------------------

    @Test
    void getWidgetData_habitTracker_calculatesDoneAndStreak() {
        ObjectNode settings = objectMapper.createObjectNode().put("showMax", 1);
        DashboardWidgetEntity widget = baseWidget("habit_tracker", settings);

        when(widgetRepository.findOwnedWidget(userId, widgetId))
                .thenReturn(Optional.of(widget));
        when(moduleRepository.findById(moduleId))
                .thenReturn(Optional.of(module("habit_tracker")));

        HabitEntity habit = HabitEntity.builder()
                .id(UUID.randomUUID())
                .title("Drink water")
                .build();

        when(habitRepository.findByUserIdAndIsActiveTrueOrderByCreatedAtDesc(userId))
                .thenReturn(List.of(habit));

        LocalDate today = LocalDate.now();
        when(habitLogRepository.findByHabitIdAndLoggedDate(habit.getId(), today))
                .thenReturn(Optional.of(mock(HabitLogEntity.class)));

        when(habitLogRepository.findRecentByHabit(eq(habit.getId()), any(PageRequest.class)))
                .thenReturn(List.of(
                        HabitLogEntity.builder().loggedDate(today).build(),
                        HabitLogEntity.builder().loggedDate(today.minusDays(1)).build()
                ));

        Object out = service.getWidgetData(userId, widgetId);

        assertTrue(out instanceof HabitTrackerWidgetData);
        HabitTrackerWidgetData data = (HabitTrackerWidgetData) out;

        assertEquals(1, data.habits().size());
        HabitTrackerWidgetData.Item item = data.habits().get(0);
        assertTrue(item.doneToday());
        assertEquals(2, item.streak());
    }

    // -------------------------------------------------------------------------
    // helpers
    // -------------------------------------------------------------------------

    private DashboardWidgetEntity baseWidget(String moduleCode) {
        return baseWidget(moduleCode, objectMapper.createObjectNode());
    }

    private DashboardWidgetEntity baseWidget(String moduleCode, ObjectNode settings) {
        return DashboardWidgetEntity.builder()
                .id(widgetId)
                .moduleId(moduleId)
                .settings(settings)
                .build();
    }

    private ModuleEntity module(String code) {
        return ModuleEntity.builder()
                .id(moduleId)
                .code(code)
                .build();
    }
}
