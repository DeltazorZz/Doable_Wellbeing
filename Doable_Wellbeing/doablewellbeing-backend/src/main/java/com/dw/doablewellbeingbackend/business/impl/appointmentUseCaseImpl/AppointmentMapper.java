package com.dw.doablewellbeingbackend.business.impl.appointmentUseCaseImpl;

import com.dw.doablewellbeingbackend.domain.appointment.AppointmentView;
import com.dw.doablewellbeingbackend.presistence.entity.AppointmentEntity;

public class AppointmentMapper {
    static AppointmentView toView(AppointmentEntity e) {
        return AppointmentView.builder()
                .id(e.getId())
                .coachId(e.getCoachId())
                .clientId(e.getClientId())
                .startsAt(e.getStartsAt())
                .endsAt(e.getEndsAt())
                .status(e.getStatus().name())
                .notes(e.getNotes())
                .build();
        }
}
