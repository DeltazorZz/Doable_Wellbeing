package com.dw.doablewellbeingbackend.business.appointmentUseCases;

import java.util.UUID;

public interface BookingGuard {

    void assertUserCanBook(UUID userId);

}
