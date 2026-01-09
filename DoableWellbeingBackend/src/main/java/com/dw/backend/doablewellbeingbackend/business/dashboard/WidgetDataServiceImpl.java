package com.dw.backend.doablewellbeingbackend.business.dashboard;

import com.dw.backend.doablewellbeingbackend.domain.dashboard.*;
import com.dw.backend.doablewellbeingbackend.persistence.entity.*;
import com.dw.backend.doablewellbeingbackend.persistence.impl.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Pageable;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WidgetDataServiceImpl implements WidgetDataService {

    private final HabitRepository habitRepository;
    private final HabitLogRepository habitLogRepository;
    private final MoodLogRepository moodLogRepository;
    private final AppointmentRepository appointmentRepository;
    private final DashboardWidgetRepository widgetRepository;
    private final ModuleRepository moduleRepository;
    private final AppointmentNoteRepository appointmentNoteRepository;
    private final AppointmentResourceRepository appointmentResourceRepository;

    @Override
    @Transactional(readOnly = true)
    public Object getWidgetData(UUID userId, UUID widgetId){
        DashboardWidgetEntity widget = widgetRepository.findOwnedWidget(userId, widgetId)
                .orElseThrow(() -> new IllegalArgumentException("Widget not found or access denied"));

        ModuleEntity module = moduleRepository.findById(widget.getModuleId())
                .orElseThrow(() -> new IllegalStateException("Module missing for widget"));

        String code = module.getCode();

        return switch (code){
            case "upcoming_meetings" -> buildUpcomingMeetings(widget, userId);
            case "completed_meetings" -> buildCompletedMeetings(widget, userId);
            case "mood_chart" -> buildMoodChart(widget, userId);
            case "habit_tracker" -> buildHabitTracker(widget, userId);
            default -> new UnknownWidgetData(code, "No data provider wired yet for this widget.");
        };



    }

    private UpcomingMeetingsWidgetData buildUpcomingMeetings(DashboardWidgetEntity widget, UUID userId) {
        int showDaysAhead = widget.getSettings().path("showDaysAhead").asInt(14);

        OffsetDateTime from = OffsetDateTime.now();
        OffsetDateTime to = from.plusDays(showDaysAhead);

        List<AppointmentEntity> appts = appointmentRepository.findUpcomingForClient(userId, from, to);


        return new UpcomingMeetingsWidgetData(
                showDaysAhead,
                appts.stream().map(a ->
                        new UpcomingMeetingsWidgetData.MeetingItem(
                                a.getId().toString(),
                                "Coaching session",
                                a.getStartsAt().toString(),
                                a.getEndsAt().toString(),
                                a.getStatus().name(),
                                a.getMeetingUrl()
                        )
                ).toList()
        );
    }


    private CompletedMeetingsWidgetData buildCompletedMeetings(DashboardWidgetEntity widget, UUID userId) {
        int maxItems = widget.getSettings().path("maxItems").asInt(5);

        var appts = appointmentRepository.findCompletedForClient(
                userId,
                 PageRequest.of(0, maxItems)
        );

        var sessions = appts.stream().map(a -> {
            var notes = appointmentNoteRepository.findByAppointmentIdOrderByCreatedAtAsc(a.getId());
            String summary = notes.isEmpty()
                    ? null
                    : notes.stream()
                    .map(AppointmentNoteEntity::getNote)
                    .filter(s -> s != null && !s.isBlank())
                    .reduce((x, y) -> x + "\n\n" + y)
                    .orElse(null);


            var res = appointmentResourceRepository.findByAppointmentIdOrderByCreatedAtDesc(a.getId());
            var files = res.stream().map(r -> new CompletedMeetingsWidgetData.ResourceFile(
                    r.getId().toString(),
                    r.getFileName(),
                    sizeLabel(r.getSizeBytes()),
                    r.getUrl()
            )).toList();

            String title = "Coaching session";
            String dateLabel = a.getStartsAt().toLocalDate().toString();

            return new CompletedMeetingsWidgetData.CompletedSessionItem(
                    a.getId().toString(),
                    dateLabel,
                    title,
                    summary,
                    files
            );
        }).toList();

        return new CompletedMeetingsWidgetData(maxItems, sessions);
    }

    private String sizeLabel(Long sizeBytes) {
        if (sizeBytes == null || sizeBytes <= 0) return "";
        double kb = sizeBytes / 1024.0;
        if (kb < 1024) return String.format("%.0f KB", kb);
        double mb = kb / 1024.0;
        if (mb < 1024) return String.format("%.1f MB", mb);
        double gb = mb / 1024.0;
        return String.format("%.1f GB", gb);
    }

    private MoodChartWidgetData buildMoodChart(DashboardWidgetEntity widget, UUID userId) {
        int rangeDays = widget.getSettings().path("rangeDays").asInt(14);
        OffsetDateTime after = OffsetDateTime.now().minusDays(rangeDays);

        var logs = moodLogRepository.findRecent(userId, after);

        return new MoodChartWidgetData(
                rangeDays,
                logs.stream().map(l -> new MoodChartWidgetData.Point(l.getLoggedAt().toString(), l.getMoodScore())).toList()
        );
    }


    private HabitTrackerWidgetData buildHabitTracker(DashboardWidgetEntity widget, UUID userId) {
        int showMax = widget.getSettings().path("showMax").asInt(6);
        LocalDate today = LocalDate.now();

        var habits = habitRepository.findByUserIdAndIsActiveTrueOrderByCreatedAtDesc(userId)
                .stream().limit(showMax).toList();

        var items = habits.stream().map(h -> {
            boolean doneToday = habitLogRepository.findByHabitIdAndLoggedDate(h.getId(), today).isPresent();


            var logs = habitLogRepository.findRecentByHabit(h.getId(), PageRequest.of(0, 60));
            int streak = 0;
            LocalDate cursor = today;

            if (!doneToday) cursor = today.minusDays(1);

            var logDates = logs.stream().map(HabitLogEntity::getLoggedDate).collect(Collectors.toSet());
            while (logDates.contains(cursor)) {
                streak++;
                cursor = cursor.minusDays(1);
            }

            return new HabitTrackerWidgetData.Item(h.getId().toString(), h.getTitle(), doneToday, streak);
        }).toList();

        return new HabitTrackerWidgetData(showMax, items);
    }

}
