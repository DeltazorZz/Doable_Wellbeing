package com.dw.backend.doablewellbeingbackend.business.dashboard;

import com.dw.backend.doablewellbeingbackend.domain.dashboard.ModuleView;

import java.util.ArrayList;
import java.util.List;

public interface ModuleService {
    List<ModuleView> getEnabledModules();
}
