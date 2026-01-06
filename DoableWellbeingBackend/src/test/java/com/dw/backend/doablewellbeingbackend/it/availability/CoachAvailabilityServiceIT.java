package com.dw.backend.doablewellbeingbackend.it.availability;

import com.dw.backend.doablewellbeingbackend.business.availability.CoachAvailabilityService;
import com.dw.backend.doablewellbeingbackend.domain.appointment.CoachAvailabilityRequest;
import com.dw.backend.doablewellbeingbackend.domain.appointment.CoachAvailabilityResponse;
import com.dw.backend.doablewellbeingbackend.domain.appointment.CoachAvailabilityView;
import com.dw.backend.doablewellbeingbackend.it.IntegrationTestBase;
import com.dw.backend.doablewellbeingbackend.it.TestSeed;
import com.dw.backend.doablewellbeingbackend.persistence.entity.CoachAvailability;
import com.dw.backend.doablewellbeingbackend.persistence.impl.CoachAvailabilityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class CoachAvailabilityServiceIT extends IntegrationTestBase {

    @Autowired CoachAvailabilityService availabilityService;
    @Autowired CoachAvailabilityRepository availabilityRepository;
    @Autowired JdbcTemplate jdbc;

    UUID coachId;

    @BeforeEach
    void seed() {
        // minimal seed: role + user + coach row
        TestSeed.ensureRole(jdbc, "coach");
        coachId = TestSeed.insertUser(jdbc, "coach_av_it@test.com", "Coach", "IT".getBytes(StandardCharsets.UTF_8), "John", "Doe");
        TestSeed.assignRole(jdbc, coachId, "coach");
        TestSeed.insertCoach(jdbc, coachId);
    }

    @Test
    void createAvailability_recurring_repeatWeeks_createsSeries() {
        LocalDate baseDate = LocalDate.now().plusDays(3);
        CoachAvailabilityRequest req = new CoachAvailabilityRequest();
        req.setDate(baseDate);
        req.setStartTime(LocalTime.of(9, 0));
        req.setEndTime(LocalTime.of(11, 0));
        req.setRecurring(true);
        req.setRepeatWeeks(3);

        List<CoachAvailabilityView> out = availabilityService.createAvailability(coachId, req);

        assertThat(out).hasSize(3);
        assertThat(out).allMatch(v -> v.isActive());
        assertThat(out).allMatch(CoachAvailabilityView::isRecurring);

        var seriesId = out.get(0).getSeriesId();
        assertThat(seriesId).isNotNull();
        assertThat(out).allMatch(v -> seriesId.equals(v.getSeriesId()));

        // DB ellenőrzés
        List<CoachAvailability> saved = availabilityRepository.findBySeriesId(seriesId);
        assertThat(saved).hasSize(3);

        assertThat(saved)
                .extracting(CoachAvailability::getDate)
                .containsExactly(baseDate, baseDate.plusWeeks(1), baseDate.plusWeeks(2));
    }

    @Test
    void setDayOff_marksInactive() {
        LocalDate d = LocalDate.now().plusDays(4);

        CoachAvailability a = CoachAvailability.builder()
                .coachId(coachId)
                .date(d)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(10, 0))
                .isRecurring(false)
                .seriesId(null)
                .isActive(true)
                .build();

        a = availabilityRepository.save(a);

        CoachAvailabilityView updated = availabilityService.setDayOff(coachId, a.getId());

        assertThat(updated.isActive()).isFalse();

        CoachAvailability reloaded = availabilityRepository.findById(a.getId()).orElseThrow();
        assertThat(reloaded.isActive()).isFalse();
    }

    @Test
    void getAvailabilitiesForCoach_ordersByDateThenStartTime() {
        LocalDate d = LocalDate.now().plusDays(6);

        availabilityRepository.save(CoachAvailability.builder()
                .coachId(coachId).date(d)
                .startTime(LocalTime.of(10, 0)).endTime(LocalTime.of(11, 0))
                .isRecurring(false).isActive(true).build());

        availabilityRepository.save(CoachAvailability.builder()
                .coachId(coachId).date(d)
                .startTime(LocalTime.of(9, 0)).endTime(LocalTime.of(10, 0))
                .isRecurring(false).isActive(true).build());

        availabilityRepository.save(CoachAvailability.builder()
                .coachId(coachId).date(d.plusDays(1))
                .startTime(LocalTime.of(8, 0)).endTime(LocalTime.of(9, 0))
                .isRecurring(false).isActive(true).build());

        List<CoachAvailabilityResponse> res =
                availabilityService.getAvailabilitiesForCoach(coachId, d, d.plusDays(2));

        assertThat(res).hasSize(3);

        assertThat(res.get(0).date()).isEqualTo(d);
        assertThat(res.get(0).startTime()).isEqualTo(LocalTime.of(9, 0));

        assertThat(res.get(1).date()).isEqualTo(d);
        assertThat(res.get(1).startTime()).isEqualTo(LocalTime.of(10, 0));

        assertThat(res.get(2).date()).isEqualTo(d.plusDays(1));
    }
}
