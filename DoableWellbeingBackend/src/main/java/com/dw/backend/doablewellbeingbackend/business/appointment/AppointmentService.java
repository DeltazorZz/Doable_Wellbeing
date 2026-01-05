package com.dw.backend.doablewellbeingbackend.business.appointment;

import com.dw.backend.doablewellbeingbackend.domain.coach.CoachCalendarEventView;
import com.dw.backend.doablewellbeingbackend.domain.dashboard.AddAppointmentNoteRequest;
import com.dw.backend.doablewellbeingbackend.domain.dashboard.AddAppointmentResourceRequest;
import com.dw.backend.doablewellbeingbackend.persistence.entity.AppointmentEntity;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface AppointmentService {

    AppointmentEntity instantBookFromSlot(
            UUID coachId,
            UUID clientId,
            OffsetDateTime slotStart,
            int durationMinutes,
            String notes
    );


    //Automatized appointment system -> Google Meet API
    AppointmentEntity confirmAppointment(UUID coachId, UUID appointmentId);

    AppointmentEntity completeAppointment(UUID coachId, UUID appointmentId);

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
    List<AppointmentEntity> getAppointmentsForCoachRange(UUID coachId, OffsetDateTime from, OffsetDateTime to);


    List<CoachCalendarEventView> getCoachCalendar(
            UUID coachId,
            OffsetDateTime from,
            OffsetDateTime to
    );

    //Cancel appoinment
    AppointmentEntity cancelAppointmentAsClient(UUID clientId, UUID appointmentId);
    AppointmentEntity cancelAppointmentAsCoach(UUID coachId, UUID appointmentId);

    //Add note + Resources
    void addNote(UUID coachId, UUID appointmentId, AddAppointmentNoteRequest req);
    UUID addResource(UUID coachId, UUID appointmentId, AddAppointmentResourceRequest req);


//    // Add session Note
//    void noShow(UUID coachId, UUID appointmentId);


}
