package com.dw.doablewellbeingbackend.presistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Data
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "clients")
public class ClientEntity {
    @Id
    private UUID userId;
}