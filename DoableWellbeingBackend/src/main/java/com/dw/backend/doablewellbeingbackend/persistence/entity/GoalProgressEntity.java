//package com.dw.backend.doablewellbeingbackend.persistence.entity;
//
//import jakarta.persistence.*;
//import lombok.*;
//import org.hibernate.annotations.UuidGenerator;
//
//import java.time.OffsetDateTime;
//import java.util.UUID;
//
//@Entity
//@Table(name = "goal_progress")
//@Getter @Setter
//@NoArgsConstructor @AllArgsConstructor
//@Builder
//public class GoalProgressEntity {
//
//    @Id
//    @UuidGenerator
//    private UUID id;
//
//    @Column(name = "goal_id", nullable = false)
//    private UUID goalId;
//
//    @Column(nullable = false)
//    private int percent; // 0–100
//
//    private String note;
//
//    @Column(name = "logged_at", nullable = false)
//    private OffsetDateTime loggedAt;
//}