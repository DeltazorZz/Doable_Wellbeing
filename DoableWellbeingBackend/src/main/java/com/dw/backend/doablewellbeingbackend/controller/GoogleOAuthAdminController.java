package com.dw.backend.doablewellbeingbackend.controller;

import com.google.api.client.auth.oauth2.TokenResponseException;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;


@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/google/oauth")
@ConditionalOnProperty(prefix = "google.calendar", name = "enabled", havingValue = "true")
public class GoogleOAuthAdminController {

    // This matches the constructor signature that takes explicit auth URL
    private static final String AUTH_BASE_URL = "https://accounts.google.com/o/oauth2/auth";

    private static final String TOKEN_URL = "https://oauth2.googleapis.com/token";

    // Calendar scopes to create/manage events
    private static final List<String> SCOPES = List.of(
            "https://www.googleapis.com/auth/calendar.events"
    );

    @Value("${google.calendar.client-id}")
    private String clientId;

    @Value("${google.calendar.client-secret}")
    private String clientSecret;

    @Value("${google.oauth.redirect-uri}")
    private String redirectUri;

    /**
     * Step 1: Redirect the browser to Google OAuth consent screen.
     *
     * Call this in your browser: GET /admin/google/oauth/start
     */
    @GetMapping("/start")
    public ResponseEntity<Void> startOAuthFlow() {

        // Use the 4-arg constructor: (authUrl, clientId, redirectUri, scopes)
        GoogleAuthorizationCodeRequestUrl url =
                new GoogleAuthorizationCodeRequestUrl(
                        AUTH_BASE_URL,
                        clientId,
                        redirectUri,
                        SCOPES
                )
                        .setAccessType("offline")
                        .setApprovalPrompt("force");

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(url.build()));
        return ResponseEntity.status(302).headers(headers).build();
    }

    /**
     * Step 2: Google redirects back here with ?code=...
     *
     * This endpoint exchanges the "code" for an access_token + refresh_token,
     * and then returns the refresh_token in the response so you can copy-paste it
     * into your application.yml.
     */
    @GetMapping("/callback")
    public ResponseEntity<?> handleCallback(
            @RequestParam(name = "code", required = false) String code,
            @RequestParam(name = "error", required = false) String error
    ) {

        if (error != null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", error,
                    "message", "Google OAuth returned an error"
            ));
        }

        if (code == null || code.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "missing_code",
                    "message", "No authorization code received from Google"
            ));
        }

        try {
            GoogleTokenResponse tokenResponse = new GoogleAuthorizationCodeTokenRequest(
                    new NetHttpTransport(),
                    GsonFactory.getDefaultInstance(),
                    TOKEN_URL,
                    clientId,
                    clientSecret,
                    code,
                    redirectUri
            ).execute();

            String accessToken = tokenResponse.getAccessToken();
            String refreshToken = tokenResponse.getRefreshToken();
            Long expiresInSec = tokenResponse.getExpiresInSeconds();

            return ResponseEntity.ok(Map.of(
                    "instruction", "Copy the refresh_token value into your application.yml under google.calendar.refresh-token",
                    "access_token", accessToken,
                    "refresh_token", refreshToken,
                    "expires_in_seconds", expiresInSec,
                    "remember", "The refresh_token is long-lived. The access_token will expire soon, but will be auto-refreshed by GoogleCalendarService."
            ));

        } catch (TokenResponseException e) {
            return ResponseEntity.status(e.getStatusCode()).body(Map.of(
                    "error", "token_exchange_failed",
                    "details", e.getDetails() != null ? e.getDetails().toString() : e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "unexpected_error",
                    "message", e.getMessage()
            ));
        }
    }
}
