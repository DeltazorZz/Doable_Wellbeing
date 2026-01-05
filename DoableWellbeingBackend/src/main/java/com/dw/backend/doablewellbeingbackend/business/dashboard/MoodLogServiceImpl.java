package com.dw.backend.doablewellbeingbackend.business.dashboard;

import com.dw.backend.doablewellbeingbackend.domain.dashboard.LogMoodRequest;
import com.dw.backend.doablewellbeingbackend.persistence.entity.MoodLogEntity;
import com.dw.backend.doablewellbeingbackend.persistence.impl.MoodLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MoodLogServiceImpl implements MoodLogService {

    private final MoodLogRepository repo;

    @Transactional
    public void log(UUID userId, LogMoodRequest req) {
        if (req.moodScore() < 1 || req.moodScore() > 10) throw new IllegalArgumentException("moodScore 1..10");

        repo.save(MoodLogEntity.builder()
                .userId(userId)
                .moodScore(req.moodScore())
                .note(req.note())
                .loggedAt(OffsetDateTime.now())
                .build());
    }

}
