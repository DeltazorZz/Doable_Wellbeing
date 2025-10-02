package com.dw.doablewellbeingbackend.business.impl.appointmentUseCaseImpl;

import com.dw.doablewellbeingbackend.business.appointmentUseCases.BookingGuard;
import com.dw.doablewellbeingbackend.business.appointmentUseCases.CreateAppointmentUseCase;
import com.dw.doablewellbeingbackend.business.services.audit.AuditService;
import com.dw.doablewellbeingbackend.business.services.notification.NotificationService;
import com.dw.doablewellbeingbackend.common.exception.ConflictException;
import com.dw.doablewellbeingbackend.common.exception.NotFoundException;
import com.dw.doablewellbeingbackend.domain.appointment.AppointmentView;
import com.dw.doablewellbeingbackend.domain.appointment.CreateAppointmentRequest;
import com.dw.doablewellbeingbackend.domain.enums.AppointmentStatus;
import com.dw.doablewellbeingbackend.presistence.entity.AppointmentEntity;
import com.dw.doablewellbeingbackend.presistence.impl.AppointmentRepository;
import com.dw.doablewellbeingbackend.presistence.impl.ClientRepository;
import com.dw.doablewellbeingbackend.presistence.impl.CoachRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateAppointmentUseCaseImpl implements CreateAppointmentUseCase {

    private final BookingGuard bookingGuard;
    private final AppointmentRepository appointmentRepo;
    private final CoachRepository coachRepo;
    private final ClientRepository clientRepo;
    private final NotificationService notif;
    private final AuditService audit;

    @Transactional
    public AppointmentView create(UUID clientId, CreateAppointmentRequest req) {
        bookingGuard.assertUserCanBook(clientId);

        var coach = coachRepo.findById(req.getCoachId())
                .orElseThrow(() -> new NotFoundException("Coach not found"));

        var overlaps = appointmentRepo.findOverlaps(coach.getUserId(), req.getStartsAt(), req.getEndsAt());
        if (!overlaps.isEmpty()) {
            throw new ConflictException("Coach already booked in this slot");
        }



        var entity = AppointmentEntity.builder()
                .coachId(coach.getUserId())
                .clientId(clientId)
                .startsAt(req.getStartsAt())
                .endsAt(req.getEndsAt())
                .status(AppointmentStatus.scheduled)
                .notes(req.getNotes())
                .build();

        entity = appointmentRepo.save(entity);

        notif.notifyAppointmentScheduled(coach.getUserId(), clientId, entity.getId());
        audit.log(clientId, "appointment_created", "appointments", entity.getId(), Map.of());


        return AppointmentMapper.toView(entity);


    }

}
