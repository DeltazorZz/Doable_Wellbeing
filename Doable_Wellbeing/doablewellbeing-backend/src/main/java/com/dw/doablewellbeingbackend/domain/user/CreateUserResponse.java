package com.dw.doablewellbeingbackend.domain.user;

import lombok.*;

import java.util.UUID;

@Data @AllArgsConstructor
@NoArgsConstructor @Builder
public class CreateUserResponse {
    private UUID id;
}
