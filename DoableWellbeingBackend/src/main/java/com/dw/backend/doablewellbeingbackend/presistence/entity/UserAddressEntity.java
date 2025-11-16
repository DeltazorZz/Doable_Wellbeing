package com.dw.backend.doablewellbeingbackend.presistence.entity;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;


@Entity
@Table(name = "user_addresses",  indexes = { @Index(name = "ix_user_addresses_postcode", columnList = "postcode")})
@Data
@NoArgsConstructor @AllArgsConstructor @Builder
@EqualsAndHashCode(of = "id")
public class UserAddressEntity {
    @Id
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID id;

    @Column(name = "user_id", nullable = false, columnDefinition = "uuid")
    private UUID userId;

    @Column(name = "line1", nullable = false)
    private String line1;

    @Column(name = "line2")
    private String line2;

    @Column(name = "line3")
    private String line3;

    @Column(name = "city", nullable = false)
    private String city;

    @Column(name = "county")
    private String county;

    @Column(name = "postcode", nullable = false)
    private String postcode;

    @Column(name = "is_primary", nullable = false)
    private boolean isPrimary;

    @Column(name = "validated", nullable = false)
    private boolean validated;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, columnDefinition = "timestamptz")
    private Instant createdAt;
}
