package com.dw.backend.doablewellbeingbackend.controller;

import com.dw.backend.doablewellbeingbackend.business.user.UserService;
import com.dw.backend.doablewellbeingbackend.common.security.PasswordHashingService;
import com.dw.backend.doablewellbeingbackend.common.security.TokenService;
import com.dw.backend.doablewellbeingbackend.domain.loginRegister.LoginRequest;
import com.dw.backend.doablewellbeingbackend.domain.loginRegister.RegisterRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final TokenService tokenService;
    private final PasswordHashingService hasher;
    private final JwtDecoder jwtDecoder;
    private final UserService userService;

    public static final String ACCESS_COOKIE = "dw_access";
    public static final String REFRESH_COOKIE = "dw_refresh";

    @Value("${app.security.cookie.secure}")
    private boolean cookieSecure;
    @Value("${app.security.cookie.samesite}")
    private String cookieSameSite;
    @Value("${app.security.cookie.domain}")
    private String cookieDomain;
    @Value("${app.security.jwt.access-minutes}")
    private long accessMinutes;
    @Value("${app.security.jwt.refresh-days}")
    private long refreshDays;

    private ResponseCookie buildCookie(String name, String value, Duration maxAge, boolean httpOnly) {
        ResponseCookie.ResponseCookieBuilder b = ResponseCookie.from(name, value)
                .httpOnly(httpOnly)
                .secure(cookieSecure)
                .path("/")
                .maxAge(maxAge)
                .sameSite(cookieSameSite);
        if (cookieDomain != null && !cookieDomain.isBlank()) b.domain(cookieDomain);
        return b.build();
    }

    private void setAuthCookies(HttpServletResponse resp, UUID userId, String email, List<String> roles) {
        clearAuthCookies(resp);
        String access = tokenService.generateAccess(userId, email, roles);
        String refresh = tokenService.generateRefresh(userId);
        resp.addHeader(HttpHeaders.SET_COOKIE,
                buildCookie(ACCESS_COOKIE, access, Duration.ofMinutes(accessMinutes), true).toString());
        resp.addHeader(HttpHeaders.SET_COOKIE,
                buildCookie(REFRESH_COOKIE, refresh, Duration.ofDays(refreshDays), true).toString());
    }

    private void clearAuthCookies(HttpServletResponse resp) {
        resp.addHeader(HttpHeaders.SET_COOKIE, buildCookie(ACCESS_COOKIE, "", Duration.ZERO, true).toString());
        resp.addHeader(HttpHeaders.SET_COOKIE, buildCookie(REFRESH_COOKIE, "", Duration.ZERO, true).toString());
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> register(@Valid @RequestBody RegisterRequest registerReq, HttpServletResponse response) {
        if (userService.existsByEmail(registerReq.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }

        byte[] salt = hasher.generateSalt();
        String hash = hasher.hash(registerReq.getPassword().toCharArray(), salt);
        var user = userService.createUser(registerReq.getEmail(), hash, salt, registerReq.getFirstName(), registerReq.getLastName());
        setAuthCookies(response, user.getId(), user.getEmail(), user.getRoleNames());
        return Map.of("userId", user.getId(), "email", user.getEmail());

    }

    @PostMapping("/login")
    public Map<String, Object> login(@Valid @RequestBody LoginRequest loginReq, HttpServletResponse response) {
        var user = userService.findByEmail(loginReq.getEmail()).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        if (!user.isActive() || user.isDeleted())
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account disabled");

        boolean ok = hasher.verify(loginReq.getPassword().toCharArray(), user.getPasswordSalt(), user.getPasswordHash());
        if (!ok) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");

        setAuthCookies(response, user.getId(), user.getEmail(), user.getRoleNames());
        return Map.of("userId", user.getId(), "email", user.getEmail());
    }

    @PostMapping("/refresh")
    public Map<String, Object> refresh(@CookieValue(value = REFRESH_COOKIE, required = false) String refresh, HttpServletResponse response) {
        if(refresh == null || refresh.isBlank()) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No refresh token");
        Jwt jwt;
        try {
            jwt = jwtDecoder.decode(refresh);
        }catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
        }
        if(!"refresh".equals(jwt.getClaimAsString("typ"))) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token type");

        UUID userId = UUID.fromString(jwt.getSubject());
        var user = userService.getRequired(userId);


        setAuthCookies(response, userId, user.getEmail(), user.getRoleNames());
        return Map.of("ok", true);

    }

    @PostMapping("/logout")
    public Map<String, Object> logout(HttpServletResponse response) {
        clearAuthCookies(response);
        return Map.of("ok", true);
    }

    @GetMapping("/me")
    public  Map<String, Object> me(@org.springframework.security.core.annotation.AuthenticationPrincipal Jwt principal) {
        if(principal == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        UUID userId = UUID.fromString(principal.getSubject());
        var user = userService.getRequired(userId);
        return  Map.of(
                "userId", user.getId(),
                "email", user.getEmail(),
                "firstName", user.getFirstName(),
                "lastName", user.getLastName(),
                "roles", user.getRoleNames()
        );
    }
















}
