package com.dw.backend.doablewellbeingbackend.domain.dashboard;

import java.util.List;

public record CompletedMeetingsWidgetData(
        int maxItems,
        List<CompletedSessionItem> sessions
) {
    public record CompletedSessionItem(
            String id,
            String dateLabel,
            String title,
            String coachSummary,
            List<ResourceFile> files
    ) {}

    public record ResourceFile(
            String id,
            String fileName,
            String sizeLabel,
            String downloadUrl
    ) {}
}