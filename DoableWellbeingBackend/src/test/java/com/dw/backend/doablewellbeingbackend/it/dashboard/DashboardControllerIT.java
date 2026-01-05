package com.dw.backend.doablewellbeingbackend.it.dashboard;

import com.dw.backend.doablewellbeingbackend.domain.dashboard.CreateDashboardWidgetRequest;
import com.dw.backend.doablewellbeingbackend.domain.dashboard.UpdatePlacementsRequest;
import com.dw.backend.doablewellbeingbackend.domain.dashboard.UpdateWidgetSettingsRequest;
import com.dw.backend.doablewellbeingbackend.it.TestSeed;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import jakarta.servlet.http.Cookie;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import org.springframework.security.oauth2.jwt.*;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        // FONTOS: legyen stabil secret a tesztekhez (>= 32 byte base64 dekód után)
        "app.security.jwt.secret=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=",
        "app.security.jwt.issuer=doable-wellbeing",
        "app.security.jwt.roles-claim=roles"
})
class DashboardControllerIT {

    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;
    @Autowired ObjectMapper om;

    private UUID userId;
    private String userToken;

    @BeforeEach
    void seed() {
        // ---- roles seed + user seed (random email -> nincs DuplicateKey) ----
        TestSeed.ensureRole(jdbc, "user");

        String email = "dash_ctrl_it_" + UUID.randomUUID() + "@test.com";
        userId = TestSeed.insertUser(jdbc, email, "Dash", "Ctrl".getBytes(StandardCharsets.UTF_8), "John", "Doe");
        TestSeed.assignRole(jdbc, userId, "user");

        // ---- modules seed (a V12 alapján: code,name,description) ----
        TestSeed.ensureModule(jdbc, "upcoming_meetings", "Upcoming meetings", "Your next sessions");
        TestSeed.ensureModule(jdbc, "mood_chart", "Mood chart", "Mood points");

        // ---- auth token (dw_access cookie) ----
        userToken = mintToken(userId, List.of("user"));
    }

    @Test
    void getDefault_requiresAuth() throws Exception {
        mvc.perform(get("/api/dashboards/default"))
                .andExpect(status().isUnauthorized()); // custom entryPoint -> 401
    }

    @Test
    void getDefault_withUserRole_ok_andCreatesDashboard() throws Exception {
        mvc.perform(get("/api/dashboards/default")
                        .cookie(dwAccess(userToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dashboardId").exists())
                .andExpect(jsonPath("$.name").value("Default Dashboard"))
                .andExpect(jsonPath("$.isDefault").value(true));
    }

    @Test
    void addWidget_thenUpdateSettings_thenDelete_flowOk() throws Exception {
        // 1) ensure default dashboard exists + read its id
        String defaultJson = mvc.perform(get("/api/dashboards/default")
                        .cookie(dwAccess(userToken)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        UUID dashboardId = UUID.fromString(om.readTree(defaultJson).path("dashboardId").asText());

        // 2) addWidget
        var req = new CreateDashboardWidgetRequest(
                "upcoming_meetings",
                null,
                om.createObjectNode().put("showDaysAhead", 14),
                1,
                0, 0, 4, "3"
        );

        String addRes = mvc.perform(post("/api/dashboards/{dashboardId}/widgets", dashboardId)
                        .cookie(dwAccess(userToken))
                        .with(csrf().asHeader()) // CookieCsrfTokenRepository header: X-XSRF-TOKEN
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.widgetId").exists())
                .andReturn().getResponse().getContentAsString();

        UUID widgetId = UUID.fromString(om.readTree(addRes).path("widgetId").asText());

        // 3) updateWidgetSettings
        var settingsReq = new UpdateWidgetSettingsRequest(
                om.createObjectNode().put("showDaysAhead", 7)
        );

        mvc.perform(put("/api/dashboards/{dashboardId}/widgets/{widgetId}/settings", dashboardId, widgetId)
                        .cookie(dwAccess(userToken))
                        .with(csrf().asHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(settingsReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ok"));

        // 4) deleteWidget (soft delete: isActive=false)
        mvc.perform(delete("/api/dashboards/{dashboardId}/widgets/{widgetId}", dashboardId, widgetId)
                        .cookie(dwAccess(userToken))
                        .with(csrf().asHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ok"));
    }

    // ---------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------

    private Cookie dwAccess(String token) {
        Cookie c = new Cookie("dw_access", token);
        c.setPath("/");
        return c;
    }

    private String mintToken(UUID subject, List<String> roles) {
        // ugyanazzal a secret-tel írunk alá, amit a jwtDecoder ellenőriz
        byte[] keyBytes = Base64.getDecoder().decode("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=");
        SecretKey key = new SecretKeySpec(keyBytes, "HmacSHA256");

        NimbusJwtEncoder encoder = new NimbusJwtEncoder(new ImmutableSecret<>(key));

        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("doable-wellbeing")
                .subject(subject.toString())
                .issuedAt(now)
                .expiresAt(now.plusSeconds(3600))
                .claim("roles", roles) // <- rolesClaim=roles, prefix ROLE_
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();

        return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}
