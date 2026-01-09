package com.dw.backend.doablewellbeingbackend.it.e2e;

import com.dw.backend.doablewellbeingbackend.business.google.GoogleCalendarService;
import com.dw.backend.doablewellbeingbackend.it.IntegrationTestBase;
import com.dw.backend.doablewellbeingbackend.it.TestSeed;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.services.calendar.model.Event;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.servlet.http.Cookie;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.time.*;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import org.springframework.security.oauth2.jwt.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class E2EFlowIT extends IntegrationTestBase {

    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;
    @Autowired ObjectMapper om;

    @MockitoBean GoogleCalendarService googleCalendarService;

    private UUID coachId;
    private UUID userId;

    private String coachToken;
    private String userToken;

    private static final String AV_BASE = "/api/coach/availabilities";
    private static final String DASH_BASE = "/api/dashboards";
    private static final String APPT_BASE = "/appointments";


    @BeforeEach
    void seed() throws Exception {

        TestSeed.ensureCoreRoles(jdbc);


        TestSeed.ensureModule(jdbc, "upcoming_meetings", "Upcoming meetings", "Your next sessions");
        TestSeed.ensureModule(jdbc, "mood_chart", "Mood chart", "Mood points");


        coachId = TestSeed.insertUser(
                jdbc,
                "coach_e2e_" + UUID.randomUUID() + "@test.com",
                "hash",
                new byte[]{1,2,3,4,5,6,7,8},
                "Coach",
                "E2E"
        );
        userId = TestSeed.insertUser(
                jdbc,
                "user_e2e_" + UUID.randomUUID() + "@test.com",
                "hash",
                new byte[]{9,9,9,9,9,9,9,9},
                "User",
                "E2E"
        );


        TestSeed.insertCoach(jdbc, coachId);
        TestSeed.insertClient(jdbc, userId);


        coachToken = mintToken(coachId, List.of("coach"));
        userToken  = mintToken(userId,  List.of("user"));

        Event e = new Event();
        e.setId("evt-x");
        e.setHangoutLink("https://meet.google.com/test");

        when(googleCalendarService.createEventWithMeet(any(), any(), any(), any(), any()))
                .thenReturn(e);
    }

    @Test
    void e2e_availability_slots_booking_and_dashboard_flow() throws Exception {
        // ------------------------------------------------------------
        // 1) COACH creates availability
        // POST /api/coach/availabilities/me
        // ------------------------------------------------------------
        LocalDate date = LocalDate.now().plusDays(2);
        String createAvailabilityBody = """
            {
              "date": "%s",
              "startTime": "09:00",
              "endTime": "12:00",
              "recurring": false,
              "repeatWeeks": 1
            }
        """.formatted(date);

        mvc.perform(post(AV_BASE + "/me")
                        .cookie(dwAccess(coachToken))
                        .with(csrf().asHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createAvailabilityBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").exists());

        // ------------------------------------------------------------
        // 2) USER fetches slots for coach
        // GET /api/coach/availabilities/{coachId}/slots?from&to&slotLengthMinutes
        // roles: user/coach/client
        // ------------------------------------------------------------
        String slotsJson = mvc.perform(get(AV_BASE + "/{coachId}/slots", coachId)
                        .cookie(dwAccess(userToken))
                        .param("from", date.toString())
                        .param("to", date.toString())
                        .param("slotLengthMinutes", "60"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].startsAt").exists())
                .andReturn().getResponse().getContentAsString();

        JsonNode slots = om.readTree(slotsJson);
        String slotStart = slots.get(0).get("startsAt").asText(); // OffsetDateTime string

        // ------------------------------------------------------------
        // 3) USER books from slot
        // POST /api/appointments/slots/book
        // roles: user/client/coach
        // ------------------------------------------------------------
        String bookBody = """
            {
              "coachId": "%s",
              "slotStart": "%s",
              "durationMinutes": 60,
              "notes": "hello e2e"
            }
        """.formatted(coachId, slotStart);

        mvc.perform(post(APPT_BASE + "/slots/book")
                        .cookie(dwAccess(userToken))
                        .with(csrf().asHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.coachId").value(coachId.toString()));

        // ------------------------------------------------------------
        // 4) DASHBOARD default -> add widget -> update settings -> delete widget
        // GET /api/dashboards/default
        // ------------------------------------------------------------
        String dashJson = mvc.perform(get(DASH_BASE + "/default")
                        .cookie(dwAccess(userToken)))
                .andExpect(status().isOk())

                .andExpect(jsonPath("$.dashboardId").exists())
                .andExpect(jsonPath("$.name").value("Default Dashboard"))
                .andExpect(jsonPath("$.isDefault").value(true))
                .andReturn().getResponse().getContentAsString();

        UUID dashboardId = UUID.fromString(om.readTree(dashJson).path("dashboardId").asText());

        // add widget
        String addWidgetBody = """
            {
              "moduleCode": "upcoming_meetings",
              "title": null,
              "settings": { "showDaysAhead": 14 },
              "x": 0,
              "y": 0,
              "w": 4,
              "h": 3,
              "breakpoint": "lg"
            }
        """;

        String addRes = mvc.perform(post(DASH_BASE + "/{dashboardId}/widgets", dashboardId)
                        .cookie(dwAccess(userToken))
                        .with(csrf().asHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addWidgetBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.widgetId").exists())
                .andReturn().getResponse().getContentAsString();

        UUID widgetId = UUID.fromString(om.readTree(addRes).path("widgetId").asText());


        String updateSettingsBody = """
            { "settings": { "showDaysAhead": 7 } }
        """;

        mvc.perform(put(DASH_BASE + "/{dashboardId}/widgets/{widgetId}/settings", dashboardId, widgetId)
                        .cookie(dwAccess(userToken))
                        .with(csrf().asHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateSettingsBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ok"));

        // delete widget
        mvc.perform(delete( DASH_BASE + "/{dashboardId}/widgets/{widgetId}", dashboardId, widgetId)
                        .cookie(dwAccess(userToken))
                        .with(csrf().asHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ok"));
    }

    // ------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------

    private Cookie dwAccess(String token) {
        Cookie c = new Cookie("dw_access", token);
        c.setPath("/");
        return c;
    }

    private String mintToken(UUID subject, List<String> roles) {
        byte[] keyBytes = Base64.getDecoder().decode("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=");
        SecretKey key = new SecretKeySpec(keyBytes, "HmacSHA256");

        NimbusJwtEncoder encoder = new NimbusJwtEncoder(new ImmutableSecret<>(key));

        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("doable-wellbeing")
                .subject(subject.toString())
                .issuedAt(now)
                .expiresAt(now.plusSeconds(3600))
                .claim("roles", roles)
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}
