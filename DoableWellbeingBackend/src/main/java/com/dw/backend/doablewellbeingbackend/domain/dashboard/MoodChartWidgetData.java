package com.dw.backend.doablewellbeingbackend.domain.dashboard;

import java.util.List;

public record MoodChartWidgetData(
        int rangeDays,
        List<Point> points
) {
    public record Point(String at, int score) {}
}