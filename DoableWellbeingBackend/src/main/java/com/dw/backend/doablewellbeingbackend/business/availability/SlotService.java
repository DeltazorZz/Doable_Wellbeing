package com.dw.backend.doablewellbeingbackend.business.availability;

import com.dw.backend.doablewellbeingbackend.domain.appointment.SlotView;
import com.dw.backend.doablewellbeingbackend.domain.appointment.TimeSlotResponse;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

public interface SlotService {
    List<SlotView> getSlotsForCoach(
            UUID coachId,
            LocalDate fromDate,
            LocalDate toDate,
            int slotLengthMinutes,
            ZoneId coachZone
    );
}
