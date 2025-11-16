package com.dw.backend.doablewellbeingbackend.business.appointment;


import com.dw.backend.doablewellbeingbackend.common.exception.ConflictException;
import com.dw.backend.doablewellbeingbackend.common.exception.ForbiddenException;
import com.dw.backend.doablewellbeingbackend.common.exception.NotFoundException;
import com.dw.backend.doablewellbeingbackend.domain.appointment.AppointmentView;
import com.dw.backend.doablewellbeingbackend.domain.appointment.CreateAppointmentRequest;
import com.dw.backend.doablewellbeingbackend.domain.enums.AppointmentStatus;
import com.dw.backend.doablewellbeingbackend.presistence.entity.AppointmentEntity;
import com.dw.backend.doablewellbeingbackend.presistence.impl.AppointmentRepository;
import com.dw.backend.doablewellbeingbackend.presistence.impl.CoachRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    //private final SessionNoteRepository sessionNoteRepository;
    //private final AuditService auditService;
    //private final NotificationService notif;
    private final BookingGuard bookingGuard;
    private final CoachRepository coachRepo;
    //private final ClientRepository clientRepo;


    //                           //
    //Session note implementation//
    //                           //
    @Transactional
    public void addNote(UUID coachId, UUID appointmentId, String note) {
        var appt = appointmentRepository.findById(appointmentId).orElseThrow();
        if (!appt.getCoachId().equals(coachId)) throw new ForbiddenException("Not your appointment");

//        var entity = SessionNoteEntity.builder()
//                .appointmentId(appointmentId)
//                .coachId(coachId)
//                .note(note)
//                .build();
//        sessionNoteRepository.save(entity);
//        auditService.log(coachId, "session_note_added", "appointments", appointmentId, Map.of("noteId", entity.getId()));

    }


//                                            //
// Change Appointment Status implementation//
//                                            //
    @Transactional
    public void clientCancel(UUID clientId, UUID id){
        var appt = appointmentRepository.findById(id).orElseThrow(() -> new RuntimeException("Appointment not found"));
        if(!appt.getClientId().equals(clientId)) throw new ForbiddenException("Not your appointment");

        appt.setStatus(AppointmentStatus.cancelled);
        appointmentRepository.save(appt);

//        notif.notifyAppointmentCancelled(appt.getCoachId(), clientId, id);
//        auditService.log(clientId, "appointment_cancelled", "appointments", id, Map.of());
    }

    @Transactional
    public void coachCancel(UUID coachId, UUID id){
        var appt = appointmentRepository.findById(id).orElseThrow(() -> new RuntimeException("Appointment not found"));
        if(!appt.getCoachId().equals(coachId)) throw new ForbiddenException("Not your appointment");

        appt.setStatus(AppointmentStatus.cancelled);
        appointmentRepository.save(appt);

//        notif.notifyAppointmentCancelled( coachId, appt.getClientId(), id);
//        auditService.log(coachId, "appointment_cancelled", "appointments", id, Map.of());
    }

    @Transactional
    public void complete(UUID coachId, UUID id){
        var appt = appointmentRepository.findById(id).orElseThrow(() -> new RuntimeException("Appointment not found"));
        if (!appt.getCoachId().equals(coachId)) throw new ForbiddenException("Not your appointment");


        appt.setStatus(AppointmentStatus.completed);
        appointmentRepository.save(appt);

//        notif.notifyAppointmentCompleted(coachId, appt.getClientId(), id);
//        auditService.log(coachId, "appointment_completed", "appointments", id, Map.of());
    }

    @Transactional
    public void noShow(UUID coachId, UUID id){
        var appt = appointmentRepository.findById(id).orElseThrow(() -> new RuntimeException("Appointment not found"));
        if(!appt.getCoachId().equals(coachId)) throw new ForbiddenException("Not your appointment");

        appt.setStatus(AppointmentStatus.no_show);
        appointmentRepository.save(appt);

//        notif.notifyAppointmentNoShow(coachId, appt.getClientId(), id);
//        auditService.log(coachId, "appointment_noShow", "appointments", id, Map.of());

    }


    //                                 //
    //Create Appointment implementation//
    //                                 //
    @Transactional
    public AppointmentView create(UUID clientId, CreateAppointmentRequest req) {
        bookingGuard.assertUserCanBook(clientId);

        var coach = coachRepo.findById(req.getCoachId())
                .orElseThrow(() -> new NotFoundException("Coach not found"));

        var overlaps = appointmentRepository.findOverlaps(coach.getUserId(), req.getStartsAt(), req.getEndsAt());
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

        entity = appointmentRepository.save(entity);

//        notif.notifyAppointmentScheduled(coach.getUserId(), clientId, entity.getId());
//        auditService.log(clientId, "appointment_created", "appointments", entity.getId(), Map.of());


        return AppointmentMapper.toView(entity);


    }

    //                              //
    //Get Appointment implementation//
    //                              //
    @Override
    public List<AppointmentView> forClient(UUID clientId) {
        return null;
    }

    @Override
    public List<AppointmentView> forCoach(UUID coachId) {
        coachRepo.findById(coachId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Coach not found: " + coachId));

        return appointmentRepository
                .findAllByCoachIdOrderByStartsAtDesc(coachId)
                .stream()
                .map(AppointmentMapper::toView)
                .toList();
    }



}
