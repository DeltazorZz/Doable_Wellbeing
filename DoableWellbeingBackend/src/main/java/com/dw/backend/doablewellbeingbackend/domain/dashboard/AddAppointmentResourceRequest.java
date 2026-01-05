package com.dw.backend.doablewellbeingbackend.domain.dashboard;

public record AddAppointmentResourceRequest(
        String fileName,
        Long sizeBytes,
        String mimeType,
        String url
) {}
