package com.dw.backend.doablewellbeingbackend.domain.dashboard;

import java.util.List;

public record HabitTrackerWidgetData(
        int showMax,
        List<Item> habits
) {
    public record Item(
            String id,
            String title,
            boolean doneToday,
            int streak
    ) {}
}