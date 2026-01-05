package com.dw.backend.doablewellbeingbackend.business.dashboard;

import com.dw.backend.doablewellbeingbackend.domain.dashboard.ModuleView;
import com.dw.backend.doablewellbeingbackend.persistence.entity.ModuleEntity;
import com.dw.backend.doablewellbeingbackend.persistence.impl.ModuleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ModuleServiceImplTest {

    @Mock
    ModuleRepository moduleRepository;

    private ModuleServiceImpl service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        service = new ModuleServiceImpl(moduleRepository);
    }

    // -------------------------------------------------------------------------
    // getEnabledModules
    // -------------------------------------------------------------------------

    @Test
    void getEnabledModules_filtersOnlyEnabled_andMapsCorrectly() {
        ModuleEntity enabled1 = ModuleEntity.builder()
                .id(UUID.randomUUID())
                .code("upcoming_meetings")
                .name("Upcoming Meetings")
                .description("Shows future meetings")
                .isEnabled(true)
                .build();

        ModuleEntity disabled = ModuleEntity.builder()
                .id(UUID.randomUUID())
                .code("admin_only")
                .name("Admin")
                .description("Hidden")
                .isEnabled(false)
                .build();

        ModuleEntity enabled2 = ModuleEntity.builder()
                .id(UUID.randomUUID())
                .code("completed_meetings")
                .name("Completed Meetings")
                .description("Shows past meetings")
                .isEnabled(true)
                .build();

        when(moduleRepository.findAll()).thenReturn(List.of(enabled1, disabled, enabled2));

        List<ModuleView> out = service.getEnabledModules();

        assertEquals(2, out.size());

        ModuleView v1 = out.get(0);
        assertEquals(enabled1.getId(), v1.id());
        assertEquals("upcoming_meetings", v1.code());
        assertEquals("Upcoming Meetings", v1.name());
        assertEquals("Shows future meetings", v1.description());

        ModuleView v2 = out.get(1);
        assertEquals(enabled2.getId(), v2.id());
        assertEquals("completed_meetings", v2.code());
        assertEquals("Completed Meetings", v2.name());
        assertEquals("Shows past meetings", v2.description());

        verify(moduleRepository).findAll();
    }

    @Test
    void getEnabledModules_allDisabled_returnsEmptyList() {
        ModuleEntity disabled1 = ModuleEntity.builder()
                .id(UUID.randomUUID())
                .code("a")
                .isEnabled(false)
                .build();

        ModuleEntity disabled2 = ModuleEntity.builder()
                .id(UUID.randomUUID())
                .code("b")
                .isEnabled(false)
                .build();

        when(moduleRepository.findAll()).thenReturn(List.of(disabled1, disabled2));

        List<ModuleView> out = service.getEnabledModules();

        assertNotNull(out);
        assertTrue(out.isEmpty());
    }

    @Test
    void getEnabledModules_emptyRepository_returnsEmptyList() {
        when(moduleRepository.findAll()).thenReturn(List.of());

        List<ModuleView> out = service.getEnabledModules();

        assertNotNull(out);
        assertTrue(out.isEmpty());
    }
}
