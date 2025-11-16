package com.dw.backend.doablewellbeingbackend.common.security;

import com.nimbusds.jwt.JWTClaimsSet;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.SignedJWT;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
public class TokenService {

    @Value("${app.security.jwt.secret}")
    private String secretB64;

    @Value("${app.security.jwt.issuer}")
    private String issuer;

    @Value("${app.security.jwt.access-minutes}")
    private int accessMinutes;

    @Value("${app.security.jwt.refresh-days}")
    private int refreshDays;

    @Value("${app.security.jwt.roles-claim}")
    private String rolesClaim;


    private SecretKey key;



    @PostConstruct
    void init() {
        byte[] bytes;
        try{
            bytes = Base64.getDecoder().decode(secretB64);
        }catch(IllegalArgumentException e){
            bytes = secretB64.getBytes();
        }
        if (bytes.length < 32) {
            throw new IllegalStateException("JWT secret too weak. Provide >= 256-bit Base64 key.");
        }
        this.key = new SecretKeySpec(bytes, "HmacSHA256");
    }

    public String generateAccess(UUID userId, String email, Collection<String> roles) {
        Instant now = Instant.now();
        JWTClaimsSet.Builder b = new JWTClaimsSet.Builder()
                .subject(userId.toString())
                .issuer(issuer)
                .issueTime(Date.from(now))
                .expirationTime(Date.from(now.plus(accessMinutes, ChronoUnit.MINUTES)))
                .claim("typ", "access")
                .claim("email", email);

        if (roles != null && !roles.isEmpty()) {
            List<String> uniqueRoles = roles.stream()
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .distinct()
                    .toList();
            b.claim(rolesClaim, uniqueRoles);
        }
        return sign(b.build());
    }

    public String generateRefresh(UUID userId){
        Instant now = Instant.now();
        JWTClaimsSet jwt = new JWTClaimsSet.Builder()
                .subject(userId.toString())
                .issuer(issuer)
                .issueTime(Date.from(now))
                .expirationTime(Date.from(now.plus(refreshDays, ChronoUnit.DAYS)))
                .jwtID(UUID.randomUUID().toString())
                .claim("typ", "refresh")
                .build();

        return sign(jwt);
    }


    public String sign(JWTClaimsSet claims){
        try {
            SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
            jwt.sign(new MACSigner(key.getEncoded()));
            return jwt.serialize();
        }catch(Exception e){
            throw new IllegalStateException("JWT signing failed.", e);
        }

    }

}
