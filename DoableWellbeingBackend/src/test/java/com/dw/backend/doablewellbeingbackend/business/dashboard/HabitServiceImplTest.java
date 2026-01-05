package com.dw.backend.doablewellbeingbackend.business.dashboard;

import com.dw.backend.doablewellbeingbackend.domain.dashboard.CreateHabitRequest;
import com.dw.backend.doablewellbeingbackend.persistence.entity.HabitEntity;
import com.dw.backend.doablewellbeingbackend.persistence.entity.HabitLogEntity;
import com.dw.backend.doablewellbeingbackend.persistence.impl.HabitLogRepository;
import com.dw.backend.doablewellbeingbackend.persistence.impl.HabitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HabitServiceImplTest {

    @Mock HabitRepository habitRepository;
    @Mock HabitLogRepository habitLogRepository;

    private HabitServiceImpl service;

    private UUID userId;
    private UUID habitId;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        service = new HabitServiceImpl(habitRepository, habitLogRepository);

        userId = UUID.randomUUID();
        habitId = UUID.randomUUID();
    }

    // -------------------------------------------------------------------------
    // createHabit
    // -------------------------------------------------------------------------

    @Test
    void createHabit_trimsTitle_andSaves_andReturnsId() {
        CreateHabitRequest req = new CreateHabitRequest("  Drink water  ");

        when(habitRepository.existsByUserIdAndTitleIgnoreCase(userId, "Drink water"))
                .thenReturn(false);

        UUID savedId = UUID.randomUUID();
        when(habitRepository.save(any())).thenAnswer(inv -> {
            HabitEntity e = inv.getArgument(0);
            // emulate DB id generation
            // if entity has setter: e.setId(savedId); return e;
            // safer: return new instance with id
            return HabitEntity.builder()
                    .id(savedId)
                    .userId(e.getUserId())
                    .title(e.getTitle())
                    .isActive(e.isActive())
                    .createdAt(e.getCreatedAt())
                    .build();
        });

        UUID out = service.createHabit(userId, req);

        assertEquals(savedId, out);

        ArgumentCaptor<HabitEntity> captor = ArgumentCaptor.forClass(HabitEntity.class);
        verify(habitRepository).save(captor.capture());

        HabitEntity saved = captor.getValue();
        assertEquals(userId, saved.getUserId());
        assertEquals("Drink water", saved.getTitle());
        assertTrue(saved.isActive());
        assertNotNull(saved.getCreatedAt());
    }

    @Test
    void createHabit_titleTooShort_throws_andDoesNotCallRepo() {
        CreateHabitRequest req = new CreateHabitRequest("  ab  "); // trimmed => "ab" (len 2)

        assertThrows(IllegalArgumentException.class, () ->
                service.createHabit(userId, req)
        );

        verifyNoInteractions(habitRepository, habitLogRepository);
    }

    @Test
    void createHabit_duplicateTitle_throws_andDoesNotSave() {
        CreateHabitRequest req = new CreateHabitRequest("Read");

        when(habitRepository.existsByUserIdAndTitleIgnoreCase(userId, "Read"))
                .thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                service.createHabit(userId, req)
        );
        assertTrue(ex.getMessage().contains("already exists"));

        verify(habitRepository, never()).save(any());
        verifyNoInteractions(habitLogRepository);
    }

    // -------------------------------------------------------------------------
    // markDoneToday
    // -------------------------------------------------------------------------

    @Test
    void markDoneToday_habitNotFound_throws() {
        when(habitRepository.findById(habitId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                service.markDoneToday(userId, habitId)
        );

        verifyNoInteractions(habitLogRepository);
    }

    @Test
    void markDoneToday_accessDenied_throws() {
        HabitEntity habit = HabitEntity.builder()
                .id(habitId)
                .userId(UUID.randomUUID()) // not the caller
                .title("Test")
                .isActive(true)
                .build();

        when(habitRepository.findById(habitId)).thenReturn(Optional.of(habit));

        assertThrows(IllegalArgumentException.class, () ->
                service.markDoneToday(userId, habitId)
        );

        verifyNoInteractions(habitLogRepository);
    }

    @Test
    void markDoneToday_alreadyLoggedToday_returnsWithoutSaving() {
        HabitEntity habit = HabitEntity.builder()
                .id(habitId)
                .userId(userId)
                .title("Test")
                .isActive(true)
                .build();

        when(habitRepository.findById(habitId)).thenReturn(Optional.of(habit));

        LocalDate today = LocalDate.now();
        when(habitLogRepository.findByHabitIdAndLoggedDate(habitId, today))
                .thenReturn(Optional.of(mock(HabitLogEntity.class)));

        service.markDoneToday(userId, habitId);

        verify(habitLogRepository, never()).save(any());
    }

    @Test
    void markDoneToday_notLoggedToday_savesNewLog() {
        HabitEntity habit = HabitEntity.builder()
                .id(habitId)
                .userId(userId)
                .title("Test")
                .isActive(true)
                .build();

        when(habitRepository.findById(habitId)).thenReturn(Optional.of(habit));

        LocalDate today = LocalDate.now();
        when(habitLogRepository.findByHabitIdAndLoggedDate(habitId, today))
                .thenReturn(Optional.empty());

        when(habitLogRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.markDoneToday(userId, habitId);

        ArgumentCaptor<HabitLogEntity> captor = ArgumentCaptor.forClass(HabitLogEntity.class);
        verify(habitLogRepository).save(captor.capture());

        HabitLogEntity saved = captor.getValue();
        assertEquals(habitId, saved.getHabitId());
        assertEquals(today, saved.getLoggedDate());
        assertNotNull(saved.getCreatedAt());
    }
}
