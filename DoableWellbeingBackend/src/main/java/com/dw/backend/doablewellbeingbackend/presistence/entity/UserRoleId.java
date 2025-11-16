
package com.dw.backend.doablewellbeingbackend.presistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode
public class UserRoleId implements Serializable {
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    // Ha roles.id INT (IDENTITY), akkor Integer/Long
    @Column(name = "role_id", nullable = false)
    private Integer roleId;
}
