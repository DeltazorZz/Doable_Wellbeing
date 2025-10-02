package com.dw.doablewellbeingbackend.business.impl.appointmentUseCaseImpl;

import com.dw.doablewellbeingbackend.business.appointmentUseCases.ChangeAppointmentStatusUseCase;
import com.dw.doablewellbeingbackend.business.services.audit.AuditService;
import com.dw.doablewellbeingbackend.business.services.notification.NotificationService;
import com.dw.doablewellbeingbackend.common.exception.ForbiddenException;
import com.dw.doablewellbeingbackend.domain.enums.AppointmentStatus;
import com.dw.doablewellbeingbackend.presistence.impl.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChangeAppointmentStatusUseCaseImpl implements ChangeAppointmentStatusUseCase {
    private final AppointmentRepository appointmentRepo;
    private final NotificationService notif;
    private final AuditService audit;

    @Transactional
    public void clientCancel(UUID clientId, UUID id){
        var appt = appointmentRepo.findById(id).orElseThrow(() -> new RuntimeException("Appointment not found"));
        if(!appt.getClientId().equals(clientId)) throw new ForbiddenException("Not your appointment");

        appt.setStatus(AppointmentStatus.cancelled);
        appointmentRepo.save(appt);

        notif.notifyAppointmentCancelled(appt.getCoachId(), clientId, id);
        audit.log(clientId, "appointment_cancelled", "appointments", id, Map.of());
    }

    @Transactional
    public void coachCancel(UUID coachId, UUID id){
        var appt = appointmentRepo.findById(id).orElseThrow(() -> new RuntimeException("Appointment not found"));
        if(!appt.getClientId().equals(coachId)) throw new ForbiddenException("Not your appointment");

        appt.setStatus(AppointmentStatus.cancelled);
        appointmentRepo.save(appt);

        notif.notifyAppointmentCancelled( coachId, appt.getClientId(), id);
        audit.log(coachId, "appointment_cancelled", "appointments", id, Map.of());
    }

    @Transactional
    public void complete(UUID coachId, UUID id){
        var appt = appointmentRepo.findById(id).orElseThrow(() -> new RuntimeException("Appointment not found"));
        if (!appt.getCoachId().equals(coachId)) throw new ForbiddenException("Not your appointment");


        appt.setStatus(AppointmentStatus.completed);
        appointmentRepo.save(appt);

        notif.notifyAppointmentCompleted(coachId, appt.getClientId(), id);
        audit.log(coachId, "appointment_completed", "appointments", id, Map.of());
    }

    @Transactional
    public void noShow(UUID coachId, UUID id){
        var appt = appointmentRepo.findById(id).orElseThrow(() -> new RuntimeException("Appointment not found"));
        if(!appt.getCoachId().equals(coachId)) throw new ForbiddenException("Not your appointment");

        appt.setStatus(AppointmentStatus.no_show);
        appointmentRepo.save(appt);

        notif.notifyAppointmentNoShow(coachId, appt.getClientId(), id);
        audit.log(coachId, "appointment_noShow", "appointments", id, Map.of());

    }


}
