package com.dw.doablewellbeingbackend.business.appointmentUseCases;

import com.dw.doablewellbeingbackend.domain.appointment.AppointmentView;
import com.dw.doablewellbeingbackend.domain.appointment.CreateAppointmentRequest;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface CreateAppointmentUseCase {

    AppointmentView create(UUID clientId, CreateAppointmentRequest req);

}
