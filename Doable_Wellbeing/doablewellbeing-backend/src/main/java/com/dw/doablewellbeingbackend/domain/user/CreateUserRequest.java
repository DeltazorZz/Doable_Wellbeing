package com.dw.doablewellbeingbackend.domain.user;

import com.dw.doablewellbeingbackend.domain.enums.GenderIdentity;
import com.dw.doablewellbeingbackend.domain.enums.Title;
import jakarta.validation.constraints.*;
import lombok.*;

@Data @AllArgsConstructor @NoArgsConstructor @Builder
public class CreateUserRequest {
    @Email @NotBlank private String email;
    @NotBlank @Size(min = 8) private String password;
    private Title title;
    @NotBlank private String firstName;
    @NotBlank private String lastName;
    private java.time.LocalDate dateOfBirth;
    private GenderIdentity genderIdentity;
    private Boolean isGenderSameAsAssignedAtBirth;
    private String nhsNumber;

}
