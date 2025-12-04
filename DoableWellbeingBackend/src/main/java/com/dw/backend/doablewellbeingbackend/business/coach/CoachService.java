package com.dw.backend.doablewellbeingbackend.business.coach;

import com.dw.backend.doablewellbeingbackend.domain.coach.CoachSummaryResponse;

import java.util.List;

public interface CoachService {
    List<CoachSummaryResponse> getAllCoaches();
}
