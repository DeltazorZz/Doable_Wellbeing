package com.dw.backend.doablewellbeingbackend.business.dashboard;

import com.dw.backend.doablewellbeingbackend.domain.dashboard.ModuleView;
import com.dw.backend.doablewellbeingbackend.persistence.entity.ModuleEntity;
import com.dw.backend.doablewellbeingbackend.persistence.impl.ModuleRepository;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ModuleServiceImpl implements ModuleService {
    private final ModuleRepository moduleRepository;

    @Override
    public List<ModuleView> getEnabledModules() {
        return moduleRepository.findAll().stream()
                .filter(ModuleEntity::isEnabled)
                .map(m -> new ModuleView(
                        m.getId(),
                        m.getCode(),
                        m.getName(),
                        m.getDescription()

                ))
                .toList();
    }
}
