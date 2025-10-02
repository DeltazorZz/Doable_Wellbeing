package com.dw.doablewellbeingbackend.business.services.notification;

import com.dw.doablewellbeingbackend.presistence.entity.NotificationEntity;
import com.dw.doablewellbeingbackend.presistence.impl.NotificationRepository;
import com.dw.doablewellbeingbackend.presistence.impl.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();


    @Override @Transactional
    public void notifyAppointmentScheduled(UUID coachId, UUID clientId, UUID appointmentId) {
        createInApp(coachId, "appointment_scheduled",
                Map.of("appointment_id", appointmentId, "role", "coach", "by", clientId));
        createInApp(clientId, "appointment_scheduled",
                Map.of("appointment_id", appointmentId, "role", "client", "with", clientId));
//        sendEmailIfPossible(coachId, "New appointment scheduled", "appointment_scheduled",
//                Map.of("appointmentId", appointmentId));
//        sendEmailIfPossible(clientId, "Your appointment is booked", "appointment_scheduled",
//                Map.of("appointmentId", appointmentId));
    }

    @Override @Transactional
    public void notifyAppointmentCancelled(UUID coachId, UUID clientId, UUID appointmentId) {
        createInApp(coachId, "appointment_cancelled",
                Map.of("appointmentId", appointmentId, "by", clientId));
        createInApp(clientId, "appointment_cancelled",
                Map.of("appointmentId", appointmentId));
//        sendEmailIfPossible(coachId, "Appointment cancelled", "appointment_cancelled",
//                Map.of("appointmentId", appointmentId));
//        sendEmailIfPossible(clientId, "Appointment cancelled", "appointment_cancelled",
//                Map.of("appointmentId", appointmentId));
    }

    @Override @Transactional
    public void notifyAppointmentCompleted(UUID coachId, UUID clientId, UUID appointmentId) {
        createInApp(clientId, "appointment_completed",
                Map.of("appointmentId", appointmentId, "by", coachId));
//        sendEmailIfPossible(clientId, "Appointment completed", "appointment_completed",
//                Map.of("appointmentId", appointmentId));
    }

    @Override @Transactional
    public void notifyAppointmentNoShow(UUID coachId, UUID clientId, UUID appointmentId) {
        createInApp(clientId, "appointment_no_show",
                Map.of("appointmentId", appointmentId));
//        sendEmailIfPossible(clientId, "Marked as no-show", "appointment_no_show",
//                Map.of("appointmentId", appointmentId));
    }

    @Override @Transactional
    public void notifyNewReferral(UUID userId, UUID referralId) {
        createInApp(userId, "new_self_referral",
                Map.of("referralId", referralId));
//        sendEmailIfPossible(userId, "Thank you for your referral", "new_self_referral",
//                Map.of("referralId", referralId));

    }


    private void createInApp(UUID userId, String type, Map<String, Object> payload) {
        var json = toJson(payload);
        var entity = NotificationEntity.builder()
                .userId(userId)
                .type(type)
                .payload(json)
                .status("queued")
                .build();
        notificationRepository.save(entity);
    }

//    private void sendEmailIfPossible(UUID userId, String subject, String template, Map<String, Object> model) {
//        if (emailSender == null) return;
//        userRepository.findById(userId).ifPresent(user -> {
//            try {
//                emailSender.send(user.getEmail(), subject, template, model);
//            } catch (Exception e) {
//                log.warn("Email send failed to {}: {}", user.getEmail(), e.getMessage());
//            }
//        });
//    }

    private String toJson(Map<String, Object> payload) {
        try { return objectMapper.writeValueAsString(payload); }
        catch (JsonProcessingException e) { return "{}"; }
    }
}
