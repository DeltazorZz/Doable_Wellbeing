package com.dw.doablewellbeingbackend.business.impl;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class PasswordHasher {
    private final BCryptPasswordEncoder enc=new BCryptPasswordEncoder();
    public String hash(String raw) { return enc.encode(raw); }
}
