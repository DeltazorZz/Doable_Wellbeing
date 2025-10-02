package com.dw.doablewellbeingbackend.business.appointmentUseCases;

import com.dw.doablewellbeingbackend.domain.appointment.AppointmentView;

import java.util.List;
import java.util.UUID;

public interface GetAppointmentUseCase {

    List<AppointmentView> forClient(UUID clientId);
    List<AppointmentView> forCoach(UUID coachId);

}
