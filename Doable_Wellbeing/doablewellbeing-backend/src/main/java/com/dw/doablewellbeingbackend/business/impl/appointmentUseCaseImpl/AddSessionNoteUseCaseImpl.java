package com.dw.doablewellbeingbackend.business.impl.appointmentUseCaseImpl;

import com.dw.doablewellbeingbackend.business.appointmentUseCases.AddSessionNoteUseCase;
import com.dw.doablewellbeingbackend.business.services.audit.AuditService;
import com.dw.doablewellbeingbackend.common.exception.ForbiddenException;
import com.dw.doablewellbeingbackend.presistence.entity.SessionNoteEntity;
import com.dw.doablewellbeingbackend.presistence.impl.AppointmentRepository;
import com.dw.doablewellbeingbackend.presistence.impl.SessionNoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AddSessionNoteUseCaseImpl implements AddSessionNoteUseCase {

    private final AppointmentRepository appointmentRepository;
    private final SessionNoteRepository sessionNoteRepository;
    private final AuditService auditService;


    @Transactional
    public void addNote(UUID coachId, UUID appointmentId, String note) {
        var appt = appointmentRepository.findById(appointmentId).orElseThrow();
        if (!appt.getCoachId().equals(coachId)) throw new ForbiddenException("Not your appointment");

        var entity = SessionNoteEntity.builder()
                .appointmentId(appointmentId)
                .coachId(coachId)
                .note(note)
                .build();
        sessionNoteRepository.save(entity);
        auditService.log(coachId, "session_note_added", "appointments", appointmentId, Map.of("noteId", entity.getId()));


    }

}
