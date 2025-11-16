package com.dw.backend.doablewellbeingbackend.domain.user;


import com.dw.backend.doablewellbeingbackend.domain.enums.GenderIdentity;
import com.dw.backend.doablewellbeingbackend.domain.enums.Title;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
