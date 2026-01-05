package com.dw.backend.doablewellbeingbackend.it.availability;

import com.dw.backend.doablewellbeingbackend.it.IntegrationTestBase;
import com.dw.backend.doablewellbeingbackend.it.TestSeed;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CoachAvailabilityControllerIT extends IntegrationTestBase {

    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;

    UUID coachId;
    UUID clientId;

    @BeforeEach
    void seed() {
        // roles
        TestSeed.ensureRole(jdbc, "coach");
        TestSeed.ensureRole(jdbc, "client");
        TestSeed.ensureRole(jdbc, "user");

        // users
        coachId = TestSeed.insertUser(
                jdbc,
                "coach_it@test.com",
                "pwHash",
                "salt".getBytes(StandardCharsets.UTF_8),
                "John",
                "Coach"
        );

        clientId = TestSeed.insertUser(
                jdbc,
                "client_it@test.com",
                "pwHash",
                "salt".getBytes(StandardCharsets.UTF_8),
                "Jane",
                "Client"
        );

        // assign roles
        TestSeed.assignRole(jdbc, coachId, "coach");
        TestSeed.assignRole(jdbc, clientId, "client");

        // domain tables
        TestSeed.insertCoach(jdbc, coachId);
        TestSeed.insertClient(jdbc, clientId);
    }

    @Test
    void createAvailabilityForMe_requiresAuth() throws Exception {
        String body = """
            {
              "date": "2026-01-10",
              "startTime": "09:00",
              "endTime": "10:00",
              "recurring": false,
              "repeatWeeks": 1
            }
        """;

        mvc.perform(post("/api/coach/availabilities/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                // a te security setupodban anonymous-ra 403 jön -> ezt várjuk
                .andExpect(status().isForbidden());
    }

    @Test
    void createAvailabilityForMe_withCoachRole_ok() throws Exception {
        String body = """
            {
              "date": "2026-01-10",
              "startTime": "09:00",
              "endTime": "10:00",
              "recurring": false,
              "repeatWeeks": 1
            }
        """;

        mvc.perform(post("/api/coach/availabilities/me")
                        .with(jwt()
                                .jwt(j -> j.subject(coachId.toString()))
                                .authorities(() -> "ROLE_coach")
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
    }

    @Test
    void createAvailabilityForMe_withClientRole_forbidden() throws Exception {
        String body = """
            {
              "date": "2026-01-10",
              "startTime": "09:00",
              "endTime": "10:00",
              "recurring": false,
              "repeatWeeks": 1
            }
        """;

        mvc.perform(post("/api/coach/availabilities/me")
                        .with(jwt()
                                .jwt(j -> j.subject(clientId.toString()))
                                .authorities(() -> "ROLE_client")
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    void getSlots_requiresAuth() throws Exception {
        LocalDate from = LocalDate.of(2026, 1, 10);
        LocalDate to = LocalDate.of(2026, 1, 12);

        mvc.perform(get("/api/coach/availabilities/{coachId}/slots", coachId)
                        .param("from", from.toString())
                        .param("to", to.toString())
                        .param("slotLengthMinutes", "60"))
                .andExpect(status().isUnauthorized());

    }

    @Test
    void getSlots_withClientRole_ok() throws Exception {
        LocalDate from = LocalDate.of(2026, 1, 10);
        LocalDate to = LocalDate.of(2026, 1, 12);

        mvc.perform(get("/api/coach/availabilities/{coachId}/slots", coachId)
                        .with(jwt()
                                .jwt(j -> j.subject(clientId.toString()))
                                .authorities(() -> "ROLE_client")
                        )
                        .param("from", from.toString())
                        .param("to", to.toString())
                        .param("slotLengthMinutes", "60"))
                .andExpect(status().isOk());
    }

    @Test
    void getSlots_withCoachRole_ok() throws Exception {
        LocalDate from = LocalDate.of(2026, 1, 10);
        LocalDate to = LocalDate.of(2026, 1, 12);

        mvc.perform(get("/api/coach/availabilities/{coachId}/slots", coachId)
                        .with(jwt()
                                .jwt(j -> j.subject(coachId.toString()))
                                .authorities(() -> "ROLE_coach")
                        )
                        .param("from", from.toString())
                        .param("to", to.toString())
                        .param("slotLengthMinutes", "60"))
                .andExpect(status().isOk());
    }
}
