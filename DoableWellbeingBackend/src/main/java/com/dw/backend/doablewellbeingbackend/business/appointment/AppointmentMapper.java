package com.dw.backend.doablewellbeingbackend.business.appointment;


import com.dw.backend.doablewellbeingbackend.domain.appointment.AppointmentView;
import com.dw.backend.doablewellbeingbackend.presistence.entity.AppointmentEntity;

public class AppointmentMapper {
    static AppointmentView toView(AppointmentEntity e) {
        return AppointmentView.builder()
                .id(e.getId())
                .coachId(e.getCoachId())
                .clientId(e.getClientId())
                .startsAt(e.getStartsAt())
                .endsAt(e.getEndsAt())
                .status(e.getStatus())
                .notes(e.getNotes())
                .build();
        }
}
