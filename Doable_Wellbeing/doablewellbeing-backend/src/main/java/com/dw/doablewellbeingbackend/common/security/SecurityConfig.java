package com.dw.doablewellbeingbackend.common.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity // ha majd @PreAuthorize-t használsz, ez kell
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // REST API-hoz tipikusan nincs szükség CSRF-re
                .csrf(csrf -> csrf.disable())
                // CORS (ha van külön frontend pl. Vite/Next.js)
                .cors(Customizer.withDefaults())
                // Minden kérés szabadon mehet (dev)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/**",
                                "/actuator/health",
                                "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html"
                        ).permitAll()
                        .anyRequest().permitAll()
                )
                // Ne legyen alap httpBasic/formLogin/logout (REST-nél felesleges)
                .httpBasic(b -> b.disable())
                .formLogin(fl -> fl.disable())
                .logout(lo -> lo.disable())
                // stateless REST
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }
}