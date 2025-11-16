package com.dw.backend.doablewellbeingbackend.business.user;


import com.dw.backend.doablewellbeingbackend.business.user.AppUser;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public record AppUserImpl(
        UUID id,
        String email,
        String firstName,
        String lastName,
        String passwordHash,
        byte[] passwordSalt,
        boolean active,
        boolean deleted,
        List<String> roles) implements AppUser{

    public AppUserImpl {
        if (passwordSalt != null) {
            passwordSalt = passwordSalt.clone();
        }
        // roles-ból nem-módosítható lista
        roles = (roles == null) ? List.of() : List.copyOf(roles);
    }

    @Override public UUID getId() { return id; }
    @Override public String getEmail() { return email; }
    @Override public String getFirstName() { return firstName; }
    @Override public String getLastName() { return lastName; }
    @Override public String getPasswordHash() { return passwordHash; }

    @Override
    public byte[] getPasswordSalt() {
        return passwordSalt == null ? null : passwordSalt.clone();
    }

    @Override public boolean isActive() { return active; }
    @Override public boolean isDeleted() { return deleted; }

    @Override
    public List<String> getRoleNames() {
        return Collections.unmodifiableList(roles);
    }
}
