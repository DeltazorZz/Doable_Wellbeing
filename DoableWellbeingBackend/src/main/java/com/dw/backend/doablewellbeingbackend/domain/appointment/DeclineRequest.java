package com.dw.backend.doablewellbeingbackend.domain.appointment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
public record DeclineRequest (String reason) {}
