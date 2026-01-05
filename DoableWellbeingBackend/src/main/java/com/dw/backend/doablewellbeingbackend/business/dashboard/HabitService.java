package com.dw.backend.doablewellbeingbackend.business.dashboard;

import com.dw.backend.doablewellbeingbackend.domain.dashboard.CreateHabitRequest;

import java.util.UUID;

public interface HabitService {
    UUID createHabit(UUID userId, CreateHabitRequest request);
    void markDoneToday(UUID userId, UUID habitId);
}
