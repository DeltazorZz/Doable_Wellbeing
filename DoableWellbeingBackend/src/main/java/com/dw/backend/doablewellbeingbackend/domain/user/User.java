package com.dw.backend.doablewellbeingbackend.domain.user;
import com.dw.backend.doablewellbeingbackend.domain.enums.GenderIdentity;
import com.dw.backend.doablewellbeingbackend.domain.enums.Title;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data @Builder
@NoArgsConstructor @AllArgsConstructor
public class User {
    private UUID id;
    private String email;
    private Title title;
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private GenderIdentity genderIdentity;
    private Boolean isGenderSameAsAssignedAtBirth;
    private String nhsNumber;
    private boolean isActive;
}
