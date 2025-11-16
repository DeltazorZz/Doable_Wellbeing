package com.dw.backend.doablewellbeingbackend.domain.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data @AllArgsConstructor
@NoArgsConstructor @Builder
public class CreateUserResponse {
    private UUID id;
}
