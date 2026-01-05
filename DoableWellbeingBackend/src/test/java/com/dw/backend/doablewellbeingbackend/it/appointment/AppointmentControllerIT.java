package com.dw.backend.doablewellbeingbackend.it.appointment;

import com.dw.backend.doablewellbeingbackend.business.google.GoogleCalendarService;
import com.dw.backend.doablewellbeingbackend.it.IntegrationTestBase;
import com.dw.backend.doablewellbeingbackend.it.TestSeed;
import com.google.api.services.calendar.model.Event;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AppointmentControllerIT extends IntegrationTestBase {

    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;

    @MockitoBean
    GoogleCalendarService googleCalendarService;

    UUID coachId;
    UUID clientId;

    @BeforeEach
    void seed() {
        TestSeed.ensureRole(jdbc, "coach");
        TestSeed.ensureRole(jdbc, "client");

        coachId = TestSeed.insertUser(jdbc, "coach2@test.com", "Coach", "One".getBytes(StandardCharsets.UTF_8), "John", "Doe");
        clientId = TestSeed.insertUser(jdbc, "client2@test.com", "Client", "Two".getBytes(StandardCharsets.UTF_8),"Jane", "Doe");

        TestSeed.assignRole(jdbc, coachId, "coach");
        TestSeed.assignRole(jdbc, clientId, "client");

        TestSeed.insertCoach(jdbc, coachId);
        TestSeed.insertClient(jdbc, clientId);


        Event e = new Event();
        e.setId("evt-x");
        e.setHangoutLink("https://meet.google.com/test");
        try {
            when(googleCalendarService.createEventWithMeet(any(), any(), any(), any(), any()))
                    .thenReturn(e);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Test
    void bookFromSlot_requiresAuth() throws Exception {
        mvc.perform(post("/appointments/slots/book")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());

    }

    @Test
    void bookFromSlot_clientRole_ok() throws Exception {
        OffsetDateTime slotStart = OffsetDateTime.now().plusDays(2).withSecond(0).withNano(0);

        String body = """
            {
              "coachId": "%s",
              "slotStart": "%s",
              "durationMinutes": 60,
              "notes": "hello"
            }
        """.formatted(coachId, slotStart);

        mvc.perform(post("/appointments/slots/book")
                        .with(jwt()
                                .jwt(j -> j.subject(clientId.toString()))
                                // a te security-d lehet roles claimből dolgozik;
                                // Spring Security testben legegyszerűbb: authorities
                                .authorities(() -> "ROLE_client")
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.coachId").value(coachId.toString()));
    }
}
