package com.dw.backend.doablewellbeingbackend.business.appointment;

import java.util.UUID;

public interface BookingGuard {

    void assertUserCanBook(UUID userId);

}
