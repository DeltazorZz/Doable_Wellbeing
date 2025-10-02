package com.dw.doablewellbeingbackend.business.appointmentUseCases;

import java.util.UUID;

public interface AddSessionNoteUseCase {

    void addNote(UUID coachId, UUID appointmentId, String note);

}
