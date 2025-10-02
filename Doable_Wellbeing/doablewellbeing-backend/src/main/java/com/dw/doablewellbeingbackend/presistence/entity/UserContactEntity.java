package com.dw.doablewellbeingbackend.presistence.entity;


import com.dw.doablewellbeingbackend.domain.enums.ContactType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_contacts", uniqueConstraints = { @UniqueConstraint(name = "uq_user_contacts_value", columnNames = {"user_id", "type", "value"})})
@Data
@NoArgsConstructor @AllArgsConstructor @Builder
@EqualsAndHashCode(of = "id")
public class UserContactEntity {

    @Id
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID id;

    @Column(name = "user_id", nullable = false, columnDefinition = "uuid")
    private UUID userId;

    // Postgres ENUM: contact_type
    @Column(name = "type", nullable = false, columnDefinition = "contact_type")
    private ContactType type;

    @Column(name = "value", nullable = false)
    private String value;

    @Column(name = "consent_contact", nullable = false)
    private boolean consentContact;

    @Column(name = "consent_sms", nullable = false)
    private boolean consentSms;

    @Column(name = "consent_voicemail", nullable = false)
    private boolean consentVoicemail;

    @Column(name = "is_primary", nullable = false)
    private boolean isPrimary;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, columnDefinition = "timestamptz")
    private Instant createdAt;
}
