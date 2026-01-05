//package com.dw.backend.doablewellbeingbackend.persistence.impl;
//
//import com.dw.backend.doablewellbeingbackend.persistence.entity.GoalEntity;
//import org.springframework.data.jpa.repository.JpaRepository;
//
//import java.util.List;
//import java.util.UUID;
//
//public interface GoalRepository extends JpaRepository<GoalEntity, UUID> {
//    List<GoalEntity> findByUserIdOrderByTargetDateAsc(UUID userId);
//}