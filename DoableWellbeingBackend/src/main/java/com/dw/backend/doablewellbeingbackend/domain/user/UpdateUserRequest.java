package com.dw.backend.doablewellbeingbackend.domain.user;

import com.dw.backend.doablewellbeingbackend.domain.enums.GenderIdentity;
import com.dw.backend.doablewellbeingbackend.domain.enums.Title;
import lombok.Data;

import java.time.LocalDate;


@Data
public class UpdateUserRequest {

    private Title title;
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private GenderIdentity genderIdentity;
    private Boolean isGenderSameAsAssignedAtBirth;
    private String nhsNumber;
    private Boolean isActive;

}
