package com.dw.doablewellbeingbackend.business.appointmentUseCases;

import java.util.UUID;

public interface ChangeAppointmentStatusUseCase {

    void clientCancel(UUID clientId, UUID appointmentId);
    void coachCancel(UUID coachId, UUID appointmentId);
    void complete(UUID coachId, UUID appointmentId);
    void noShow(UUID coachId, UUID appointmentId);

}
