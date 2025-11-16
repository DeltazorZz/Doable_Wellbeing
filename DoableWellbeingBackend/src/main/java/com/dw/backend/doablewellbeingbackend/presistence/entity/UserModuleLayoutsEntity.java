package com.dw.backend.doablewellbeingbackend.presistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_module_layouts", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "name"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserModuleLayoutsEntity {
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(columnDefinition = "uuid") private UUID id;

    @Column(name="user_id", nullable=false, columnDefinition="uuid")
    private UUID userId;

    @Column(nullable=false) private String name = "default";

    @Column(name="grid_lg",  nullable=false, columnDefinition="jsonb") private String gridLg  = "[]";
    @Column(name="grid_md",  nullable=false, columnDefinition="jsonb") private String gridMd  = "[]";
    @Column(name="grid_sm",  nullable=false, columnDefinition="jsonb") private String gridSm  = "[]";
    @Column(name="grid_xs",  nullable=false, columnDefinition="jsonb") private String gridXs  = "[]";
    @Column(name="grid_xxs", nullable=false, columnDefinition="jsonb") private String gridXxs = "[]";

    @Column(name="created_at", insertable=false, updatable=false)
    private OffsetDateTime createdAt;

    @Column(name="updated_at", insertable=false, updatable=false)
    private OffsetDateTime updatedAt;
}
