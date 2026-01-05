package com.dw.backend.doablewellbeingbackend.domain.dashboard;

import java.util.List;

public record UpcomingMeetingsWidgetData(
        int showDaysAhead,
        List<MeetingItem> meetings
) {
    public record MeetingItem(
            String id,
            String title,
            String startsAt,
            String endsAt,
            String status,
            String meetingUrl
    ){}
}
