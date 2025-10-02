package com.dw.doablewellbeingbackend.presistence.entity;

import jakarta.persistence.Id;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;


@Data
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "coaches")
public class CoachEntity {
    @Id
    private UUID userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private UserEntity user;

    private String bio;
    private String expertise;
    private String timezone;
}
