//package com.dw.backend.doablewellbeingbackend.persistence.entity;
//
//import jakarta.persistence.*;
//import lombok.*;
//import org.hibernate.annotations.UuidGenerator;
//
//import java.time.LocalDate;
//import java.time.OffsetDateTime;
//import java.util.UUID;
//
//@Entity
//@Table(name = "goals")
//@Getter @Setter
//@NoArgsConstructor @AllArgsConstructor
//@Builder
//public class GoalEntity {
//
//    @Id
//    @UuidGenerator
//    private UUID id;
//
//    @Column(name = "user_id", nullable = false)
//    private UUID userId;
//
//    @Column(nullable = false)
//    private String title;
//
//    private String description;
//
//    @Column(name = "target_date")
//    private LocalDate targetDate;
//
//    @Column(nullable = false)
//    private String status; // planned / in_progress / completed
//
//    @Column(name = "created_at", nullable = false)
//    private OffsetDateTime createdAt;
//}