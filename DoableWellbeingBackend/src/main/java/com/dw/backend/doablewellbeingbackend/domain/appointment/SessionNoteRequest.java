package com.dw.backend.doablewellbeingbackend.domain.appointment;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @AllArgsConstructor @NoArgsConstructor
public class SessionNoteRequest {
    @NotBlank private String note;
}
