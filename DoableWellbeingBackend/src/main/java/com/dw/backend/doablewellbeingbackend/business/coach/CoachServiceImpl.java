package com.dw.backend.doablewellbeingbackend.business.coach;

import com.dw.backend.doablewellbeingbackend.domain.coach.CoachSummaryResponse;
import com.dw.backend.doablewellbeingbackend.persistence.impl.CoachRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CoachServiceImpl implements CoachService {

    private final CoachRepository coachRepository;
    private final CoachMapper coachMapper;

    @Override
    @Transactional(readOnly = true)
    public List<CoachSummaryResponse> getAllCoaches() {
        return coachRepository.findAll()
                .stream()
                .map(coachMapper::toSummary)
                .toList();
    }

}
