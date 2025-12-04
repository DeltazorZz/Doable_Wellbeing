package com.dw.backend.doablewellbeingbackend.business.appointment;


import com.dw.backend.doablewellbeingbackend.domain.appointment.AppointmentView;
import com.dw.backend.doablewellbeingbackend.persistence.entity.AppointmentEntity;
import org.springframework.stereotype.Component;

@Component
public class AppointmentMapper {
    public AppointmentView toView(AppointmentEntity e) {
        return AppointmentView.builder()
                .id(e.getId())
                .coachId(e.getCoachId())
                .clientId(e.getClientId())
                .startsAt(e.getStartsAt())
                .endsAt(e.getEndsAt())
                .status(e.getStatus())
                .notes(e.getNotes())
                .meetingUrl(e.getMeetingUrl())
                .confirmedAt(e.getConfirmedAt())
                .build();
        }
}
