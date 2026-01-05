package com.dw.backend.doablewellbeingbackend.business.dashboard;

import com.dw.backend.doablewellbeingbackend.domain.dashboard.LogMoodRequest;

import java.util.UUID;

public interface MoodLogService {
    void log(UUID userId, LogMoodRequest req);
}
