package com.dw.doablewellbeingbackend.business.services.ports;

import java.util.Map;

public interface EmailSender {
    void send(String to, String subject, String template, Map<String, Object> model);
}
