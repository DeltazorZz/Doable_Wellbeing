package com.dw.doablewellbeingbackend.business.services.notification;

import java.util.UUID;

public interface NotificationService {

    void notifyAppointmentScheduled(UUID coachId, UUID clientId, UUID appointmentId);
    void notifyAppointmentCancelled(UUID coachId, UUID clientId, UUID appointmentId);
    void notifyAppointmentCompleted(UUID coachId, UUID clientId, UUID appointmentId);
    void notifyAppointmentNoShow(UUID coachId, UUID clientId, UUID appointmentId);
    void notifyNewReferral(UUID userId, UUID referralId);

}
