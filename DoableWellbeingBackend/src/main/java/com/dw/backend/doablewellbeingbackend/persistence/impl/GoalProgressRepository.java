//package com.dw.backend.doablewellbeingbackend.persistence.impl;
//
//import com.dw.backend.doablewellbeingbackend.persistence.entity.GoalProgressEntity;
//import org.springframework.data.jpa.repository.JpaRepository;
//
//import java.util.List;
//import java.util.UUID;
//
//public interface GoalProgressRepository extends JpaRepository<GoalProgressEntity, UUID> {
//    List<GoalProgressEntity> findByGoalIdOrderByLoggedAtDesc(UUID goalId);
//}