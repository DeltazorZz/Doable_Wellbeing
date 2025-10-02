package com.dw.doablewellbeingbackend.domain.appointment;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data @Builder
public class SessionNoteRequest {
    @NotBlank private String note;
}
