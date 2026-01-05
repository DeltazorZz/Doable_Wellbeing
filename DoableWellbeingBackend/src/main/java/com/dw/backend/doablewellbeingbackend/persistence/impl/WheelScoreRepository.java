package com.dw.backend.doablewellbeingbackend.persistence.impl;

import com.dw.backend.doablewellbeingbackend.persistence.entity.WheelScoreEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface WheelScoreRepository extends JpaRepository<WheelScoreEntity, UUID> {
    @Query("""
    select ws from WheelScoreEntity ws
    where ws.userId = :userId
    and ws.scoredAt = (
      select max(ws2.scoredAt) from WheelScoreEntity ws2
      where ws2.userId = :userId and ws2.area = ws.area
    )
  """)
    List<WheelScoreEntity> findLatestPerArea(UUID userId);
}