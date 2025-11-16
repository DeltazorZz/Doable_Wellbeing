package com.dw.backend.doablewellbeingbackend.common.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.cors.*;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${app.security.jwt.secret:}")
    private String hs256SecretB64;

    @Value("${app.security.jwt.issuer:doable-wellbeing}")
    private String issuer;

    @Value("${app.security.jwt.roles-claim:roles}")
    private String rolesClaim;

    @Value("${app.cors.allowed-origins:}")
    private List<String> allowedOrigins;

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .ignoringRequestMatchers(
                                "/auth/login", "/auth/register", "/auth/refresh", "/auth/logout"
                        )
                )
                .cors(cors -> {})
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/swagger/**", "/api-docs/**", "/v3/api-docs/**",
                                "/actuator/health", "/actuator/info", "/error").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/login", "/auth/register", "/auth/refresh", "/auth/logout").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth -> oauth
                        .jwt(jwt -> jwt
                                .decoder(jwtDecoder())
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())
                        )
                        .bearerTokenResolver(bearerTokenResolver()) // <---
                )
                .httpBasic(b -> b.disable())
                .formLogin(fl -> fl.disable())
                .logout(lo -> lo.disable())
                .exceptionHandling(eh -> eh
                        .authenticationEntryPoint((req, res, ex) -> {
                            res.setStatus(401);
                            res.setContentType("application/json");
                            res.getWriter().write("{\"error\":\"unauthorized\"}");
                        })
                        .accessDeniedHandler((req, res, ex) -> {
                            res.setStatus(403);
                            res.setContentType("application/json");
                            res.getWriter().write("{\"error\":\"forbidden\"}");
                        })
                );

        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOrigins(
                (allowedOrigins == null || allowedOrigins.isEmpty())
                        ? List.of("http://localhost:3000")
                        : allowedOrigins
        );
        cfg.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS")); // + PATCH
        cfg.setAllowedHeaders(List.of(
                "Authorization", "Content-Type", "X-Requested-With", "X-XSRF-TOKEN" // <---
        ));
        cfg.setExposedHeaders(List.of("Location"));
        cfg.setAllowCredentials(true);
        cfg.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }

    @Bean
    JwtDecoder jwtDecoder() {
        if (hs256SecretB64 == null || hs256SecretB64.isBlank()) {
            throw new IllegalStateException("No JWT secret configured; set app.security.jwt.secret (BASE64).");
        }

        byte[] keyBytes;
        try {
            keyBytes = Base64.getDecoder().decode(hs256SecretB64);
        } catch (IllegalArgumentException e) {

            keyBytes = hs256SecretB64.getBytes(StandardCharsets.UTF_8);
        }
        if (keyBytes.length < 32) { // 256 bit
            throw new IllegalStateException("JWT secret too weak. Provide at least 256-bit key (Base64).");
        }

        SecretKey key = new SecretKeySpec(keyBytes, "HmacSHA256");
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();


        OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(issuer);
        OAuth2TokenValidator<Jwt> withClockSkew = new OAuth2TokenValidator<>() {
            private final Duration skew = Duration.ofSeconds(60);
            @Override public OAuth2TokenValidatorResult validate(Jwt token) {

                return OAuth2TokenValidatorResult.success();
            }
        };
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(withIssuer, withClockSkew));

        return decoder;
    }

    private Converter<Jwt, ? extends AbstractAuthenticationToken> jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter delegate = new JwtGrantedAuthoritiesConverter();
        delegate.setAuthoritiesClaimName(rolesClaim);
        delegate.setAuthorityPrefix("ROLE_");

        return jwt -> {
            var authorities = delegate.convert(jwt);
            var principal = (jwt.getSubject() != null) ? jwt.getSubject() : "user";
            return new JwtAuthenticationToken(jwt, authorities, principal);
        };
    }



    @Bean
    BearerTokenResolver bearerTokenResolver() {
        return new BearerTokenResolver() {
            @Override
            public String resolve(HttpServletRequest request) {
                if (request.getCookies() == null) return null;
                for (var c : request.getCookies()) {
                    if ("dw_access".equals(c.getName())) {
                        return c.getValue(); // ez lesz a Bearer token
                    }
                }
                return null;
            }
        };
    }


}
