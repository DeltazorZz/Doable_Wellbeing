package com.dw.doablewellbeingbackend.business.services.audit;

import com.dw.doablewellbeingbackend.presistence.entity.AuditLogEntity;
import com.dw.doablewellbeingbackend.presistence.impl.AuditLogRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository repo;
    private final ObjectMapper mapper = new ObjectMapper();

    @Override @Transactional
    public void log(UUID userId, String action, String entity, UUID entityId, Map<String, Object> meta) {
        var json = toJson(meta == null ? Map.of() : meta);
        var e = AuditLogEntity.builder()
                .userId(userId)
                .action(action)
                .entity(entity)
                .entityId(entityId)
                .meta(json)
                .build();
        repo.save(e);
    }


    private String toJson(Map<String, Object> map) {
        try { return mapper.writeValueAsString(map); }
        catch (JsonProcessingException e) { return "{}"; }
    }
}
