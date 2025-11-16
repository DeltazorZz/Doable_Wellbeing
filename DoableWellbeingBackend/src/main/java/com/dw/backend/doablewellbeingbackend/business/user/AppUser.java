package com.dw.backend.doablewellbeingbackend.business.user;

import java.util.List;

public interface AppUser {
    java.util.UUID getId();
    String getEmail();
    String getFirstName();
    String getLastName();
    String getPasswordHash();
    byte[] getPasswordSalt();
    boolean isActive();
    boolean isDeleted();
    List<String> getRoleNames();
}
