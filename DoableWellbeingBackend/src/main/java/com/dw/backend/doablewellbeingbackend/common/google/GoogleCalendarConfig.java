package com.dw.backend.doablewellbeingbackend.common.google;


import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.*;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Configuration
public class GoogleCalendarConfig {

    @Value("${google.calendar.service-account-key-path}")
    private Resource credentialsFile;

    @Value("${google.calendar.application-name:Doable Wellbeing}")
    private String applicationName;

    @Bean
    public Calendar googleCalendar() throws IOException, GeneralSecurityException {
        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

        GoogleCredential credential = GoogleCredential
                .fromStream(credentialsFile.getInputStream())
                .createScoped(Collections.singletonList(CalendarScopes.CALENDAR));
         return new Calendar.Builder(httpTransport, jsonFactory, credential).setApplicationName(applicationName).build();
    }
}
