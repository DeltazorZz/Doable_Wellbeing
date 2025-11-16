package com.dw.backend.doablewellbeingbackend.presistence.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "roles",
        uniqueConstraints = @UniqueConstraint(name = "uq_roles_name", columnNames = "name"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RoleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, updatable = false)
    private String name; // pl. USER, COACH, ADMIN
}
