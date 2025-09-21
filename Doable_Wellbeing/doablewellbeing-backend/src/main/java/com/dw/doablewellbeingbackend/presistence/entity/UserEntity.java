package com.dw.doablewellbeingbackend.presistence.entity;

import com.dw.doablewellbeingbackend.domain.enums.GenderIdentity;
import com.dw.doablewellbeingbackend.domain.enums.Title;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Data @Builder
@NoArgsConstructor
@AllArgsConstructor
@SQLDelete(sql = "UPDATE users SET is_active = false, deleted_at = now() WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
public class UserEntity {
    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @PrePersist
    void pre() { if (id == null) id = UUID.randomUUID(); }

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    private Title title;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "date_of_birth")
    private java.time.LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender_identity")
    private GenderIdentity genderIdentity;

    @Column(name = "is_gender_same_as_assigned_at_birth")
    private Boolean isGenderSameAsAssignedAtBirth;

    @Column(name = "nhs_number", unique = true)
    private String nhsNumber;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;
}
