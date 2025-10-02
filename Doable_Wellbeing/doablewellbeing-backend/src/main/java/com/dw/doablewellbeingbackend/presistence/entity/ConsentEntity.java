package com.dw.doablewellbeingbackend.presistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "consents", indexes = { @Index(name = "ix_consents_user_type", columnList = "user_id, type, granted_at") })
@Data
@NoArgsConstructor @AllArgsConstructor @Builder
@EqualsAndHashCode(of = "id")
public class ConsentEntity {

    @Id
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID id;

    @Column(name = "user_id", nullable = false, columnDefinition = "uuid")
    private UUID userId;

    // Szabad szöveg (pl. "sms_reminders", "email_contact", stb.)
    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "granted_at", nullable = false, columnDefinition = "timestamptz")
    private Instant grantedAt;

    @Column(name = "revoked_at", columnDefinition = "timestamptz")
    private Instant revokedAt;
}
