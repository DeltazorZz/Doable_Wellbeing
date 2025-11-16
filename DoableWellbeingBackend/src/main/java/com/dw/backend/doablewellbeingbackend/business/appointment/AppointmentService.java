package com.dw.backend.doablewellbeingbackend.business.appointment;

import com.dw.backend.doablewellbeingbackend.domain.appointment.AppointmentView;
import com.dw.backend.doablewellbeingbackend.domain.appointment.CreateAppointmentRequest;

import java.util.List;
import java.util.UUID;

public interface AppointmentService {

    // Add session Note
    void addNote(UUID coachId, UUID appointmentId, String note);

    // Change appointment status
    void clientCancel(UUID clientId, UUID appointmentId);
    void coachCancel(UUID coachId, UUID appointmentId);
    void complete(UUID coachId, UUID appointmentId);
    void noShow(UUID coachId, UUID appointmentId);

    //Create appointment
    AppointmentView create(UUID clientId, CreateAppointmentRequest req);


    //Get appointment
    List<AppointmentView> forClient(UUID clientId);
    List<AppointmentView> forCoach(UUID coachId);


}
