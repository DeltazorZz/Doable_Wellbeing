package com.dw.doablewellbeingbackend.domain.user;

import com.dw.doablewellbeingbackend.domain.enums.GenderIdentity;
import com.dw.doablewellbeingbackend.domain.enums.Title;
import lombok.*;

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
