package com.dw.doablewellbeingbackend.business.services.audit;

import java.util.Map;
import java.util.UUID;

public interface AuditService {

    void log(UUID userId, String action, String entity, UUID entityId, Map<String, Object> meta);
    default void log(String action, String entity, UUID entityId, Map<String, Object> meta) {
        log(null, action, entity, entityId, meta);
    }

}
