package com.dw.backend.doablewellbeingbackend.business.dashboard;

import com.dw.backend.doablewellbeingbackend.domain.dashboard.CreateHabitRequest;
import com.dw.backend.doablewellbeingbackend.persistence.entity.HabitEntity;
import com.dw.backend.doablewellbeingbackend.persistence.entity.HabitLogEntity;
import com.dw.backend.doablewellbeingbackend.persistence.impl.HabitLogRepository;
import com.dw.backend.doablewellbeingbackend.persistence.impl.HabitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HabitServiceImpl implements HabitService {
    private final HabitRepository habitRepository;
    private final HabitLogRepository habitLogRepository;

    @Override
    @Transactional
    public UUID createHabit(UUID userId, CreateHabitRequest request) {

        String title = request.title().trim();

        if (title.length() < 3) {
            throw new IllegalArgumentException("Habit title must be at least 3 characters long");
        }


        boolean exists = habitRepository.existsByUserIdAndTitleIgnoreCase(userId, title);
        if (exists) {
            throw new IllegalArgumentException("Habit with this title already exists");
        }

        HabitEntity habit = HabitEntity.builder()
                .userId(userId)
                .title(title)
                .isActive(true)
                .createdAt(OffsetDateTime.now())
                .build();

        HabitEntity saved = habitRepository.save(habit);

        return saved.getId();
    }


    @Transactional
    public void markDoneToday(UUID userId, UUID habitId) {
        HabitEntity habit = habitRepository.findById(habitId)
                .orElseThrow(() -> new IllegalArgumentException("Habit not found"));
        if (!habit.getUserId().equals(userId)) throw new IllegalArgumentException("Access denied");

        LocalDate today = LocalDate.now();
        if (habitLogRepository.findByHabitIdAndLoggedDate(habitId, today).isPresent()) return;

        habitLogRepository
                .save(HabitLogEntity.builder()
                .habitId(habitId)
                .loggedDate(today)
                .createdAt(OffsetDateTime.now())
                .build());
    }

}
