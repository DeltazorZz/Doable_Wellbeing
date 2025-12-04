package com.dw.backend.doablewellbeingbackend.business.appointment;

import com.dw.backend.doablewellbeingbackend.domain.appointment.AppointmentView;
import com.dw.backend.doablewellbeingbackend.domain.appointment.CreateAppointmentRequest;
import com.dw.backend.doablewellbeingbackend.persistence.entity.AppointmentEntity;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface AppointmentService {


    //Automatized appointment system -> Google Meet API
    AppointmentEntity confirmAppointment(UUID coachId, UUID appointmentId);

    AppointmentEntity declineAppointment(UUID coachId, UUID appointmentId, String reason);

    AppointmentEntity requestAppointmentFromSlot(
            UUID coachId,
            UUID clientId,
            OffsetDateTime slotStart,
            int durationMinutes,
            String notes
    );

    //Get appointments for Client/Coach
    List<AppointmentEntity> getAppointmentsForClient(UUID clientId);
    List<AppointmentEntity> getAppointmentsForCoach(UUID coachId);

    //Cancel appoinment
    AppointmentEntity cancelAppointmentAsClient(UUID clientId, UUID appointmentId);
    AppointmentEntity cancelAppointmentAsCoach(UUID coachId, UUID appointmentId);

//    // Add session Note
//    void addNote(UUID coachId, UUID appointmentId, String note);
//    void complete(UUID coachId, UUID appointmentId);
//    void noShow(UUID coachId, UUID appointmentId);


}
