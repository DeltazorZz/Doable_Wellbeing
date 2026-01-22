package com.dw.backend.doablewellbeingbackend.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "appointment_resources",
        indexes = {
                @Index(name = "ix_appt_resources_appt_time", columnList = "appointment_id, created_at")
        })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentResourceEntity {

    @Id
    @UuidGenerator
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "appointment_id", nullable = false, columnDefinition = "uuid")
    private UUID appointmentId;

    @Column(name = "uploaded_by", nullable = false, columnDefinition = "uuid")
    private UUID uploadedBy;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "mime_type")
    private String mimeType;

    @Column(name = "size_bytes")
    private Long sizeBytes;

    @Column(name = "url", columnDefinition = "text")
    private String url;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;
}
