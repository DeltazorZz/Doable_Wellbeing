package com.dw.backend.doablewellbeingbackend.controller;

import com.dw.backend.doablewellbeingbackend.business.dashboard.ModuleService;
import com.dw.backend.doablewellbeingbackend.domain.dashboard.ModuleView;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/modules")
@RequiredArgsConstructor
public class ModuleController {

    private final ModuleService moduleService;

    @PreAuthorize("hasAnyRole('user', 'client')")
    @GetMapping
    public List<ModuleView> getModules() {
        return moduleService.getEnabledModules();
    }

}
