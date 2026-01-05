package com.dw.backend.doablewellbeingbackend.business.dashboard;

import com.dw.backend.doablewellbeingbackend.domain.dashboard.LogMoodRequest;
import com.dw.backend.doablewellbeingbackend.persistence.entity.MoodLogEntity;
import com.dw.backend.doablewellbeingbackend.persistence.impl.MoodLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MoodLogServiceImplTest {

    @Mock
    MoodLogRepository repo;

    private MoodLogServiceImpl service;

    private UUID userId;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        service = new MoodLogServiceImpl(repo);
        userId = UUID.randomUUID();
    }

    // -------------------------------------------------------------------------
    // log - validation
    // -------------------------------------------------------------------------

    @Test
    void log_moodScoreBelow1_throws_andDoesNotSave() {
        LogMoodRequest req = new LogMoodRequest(0, "nope");

        assertThrows(IllegalArgumentException.class, () ->
                service.log(userId, req)
        );

        verify(repo, never()).save(any());
    }

    @Test
    void log_moodScoreAbove10_throws_andDoesNotSave() {
        LogMoodRequest req = new LogMoodRequest(11, "nope");

        assertThrows(IllegalArgumentException.class, () ->
                service.log(userId, req)
        );

        verify(repo, never()).save(any());
    }

    // -------------------------------------------------------------------------
    // log - success paths
    // -------------------------------------------------------------------------

    @Test
    void log_moodScore1_savesEntity_withFields() {
        LogMoodRequest req = new LogMoodRequest(1, "bad day");

        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.log(userId, req);

        ArgumentCaptor<MoodLogEntity> captor = ArgumentCaptor.forClass(MoodLogEntity.class);
        verify(repo).save(captor.capture());

        MoodLogEntity saved = captor.getValue();
        assertEquals(userId, saved.getUserId());
        assertEquals(1, saved.getMoodScore());
        assertEquals("bad day", saved.getNote());
        assertNotNull(saved.getLoggedAt());
    }

    @Test
    void log_moodScore10_savesEntity_withFields() {
        LogMoodRequest req = new LogMoodRequest(10, "great");

        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.log(userId, req);

        ArgumentCaptor<MoodLogEntity> captor = ArgumentCaptor.forClass(MoodLogEntity.class);
        verify(repo).save(captor.capture());

        MoodLogEntity saved = captor.getValue();
        assertEquals(userId, saved.getUserId());
        assertEquals(10, saved.getMoodScore());
        assertEquals("great", saved.getNote());
        assertNotNull(saved.getLoggedAt());
    }

    @Test
    void log_noteNull_isAllowed_andStillSaves() {
        LogMoodRequest req = new LogMoodRequest(6, null);

        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.log(userId, req);

        ArgumentCaptor<MoodLogEntity> captor = ArgumentCaptor.forClass(MoodLogEntity.class);
        verify(repo).save(captor.capture());

        MoodLogEntity saved = captor.getValue();
        assertEquals(userId, saved.getUserId());
        assertEquals(6, saved.getMoodScore());
        assertNull(saved.getNote());
        assertNotNull(saved.getLoggedAt());
    }
}
