package com.dw.backend.doablewellbeingbackend.business.dashboard;

import java.util.UUID;

public interface WidgetDataService {
    Object getWidgetData(UUID userId,UUID widgetId);
}
