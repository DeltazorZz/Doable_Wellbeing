package com.dw.backend.doablewellbeingbackend.business.appointment;

import com.dw.backend.doablewellbeingbackend.common.configuration.GlobalExceptionHandler;
import com.dw.backend.doablewellbeingbackend.common.exception.ForbiddenException;
import com.dw.backend.doablewellbeingbackend.common.exception.NotFoundException;
import com.dw.backend.doablewellbeingbackend.domain.appointment.AppointmentView;
import com.dw.backend.doablewellbeingbackend.domain.appointment.CreateAppointmentRequest;
import com.dw.backend.doablewellbeingbackend.domain.enums.AppointmentStatus;
import com.dw.backend.doablewellbeingbackend.presistence.entity.AppointmentEntity;
import com.dw.backend.doablewellbeingbackend.presistence.entity.CoachEntity;
import com.dw.backend.doablewellbeingbackend.presistence.impl.AppointmentRepository;
import com.dw.backend.doablewellbeingbackend.presistence.impl.CoachRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceImplTest {

    @Mock
    private AppointmentRepository appointmentRepo;

    @InjectMocks
    private AppointmentServiceImpl service;

    @Mock
    private CoachRepository coachRepo;

    @Mock
    private BookingGuard bookingGuard;

    @Test
    void coachCancel() {
        var coachId = UUID.randomUUID();
        var id = UUID.randomUUID();

        var appt = new AppointmentEntity();
        appt.setCoachId(coachId);
        appt.setId(id);

        when(appointmentRepo.findById(id)).thenReturn(Optional.of(appt));
        when(appointmentRepo.save(appt)).thenReturn(appt);
        service.coachCancel(coachId, id);
    }
    @Test
    void coachCancel_setsCancelled_andSaves() {
        var coachId = UUID.randomUUID();
        var id = UUID.randomUUID();

        var appt = new AppointmentEntity();
        appt.setCoachId(coachId);
        appt.setId(id);
        appt.setStatus(AppointmentStatus.scheduled);


        when(appointmentRepo.findById(id)).thenReturn(Optional.of(appt));
        when(appointmentRepo.save(any(AppointmentEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        service.coachCancel(coachId, id);

        assertThat(appt.getStatus()).isEqualTo(AppointmentStatus.cancelled);

        var captor = ArgumentCaptor.forClass(AppointmentEntity.class);
        verify(appointmentRepo).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(AppointmentStatus.cancelled);

    }


    @Test
    void coachCancel_forbidden_ifCoachMismatch(){
        var coachId = UUID.randomUUID();
        var id = UUID.randomUUID();
        var wrongCoachId = UUID.randomUUID();

        var appt = new AppointmentEntity();
        appt.setCoachId(coachId);
        appt.setId(id);


        when(appointmentRepo.findById(id)).thenReturn(Optional.of(appt));
        assertThrows(ForbiddenException.class, () -> service.coachCancel(wrongCoachId, id));

        verify(appointmentRepo, never()).save(any());

    }

    @Test
    void coachCancel_notFound_ifMissing() {
        var coachId = UUID.randomUUID();
        var id = UUID.randomUUID();

        when(appointmentRepo.findById(id)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.coachCancel(coachId, id));
        verifyNoMoreInteractions(appointmentRepo);
    }

    @Test
    void create_happyPath(){
        var clientId = UUID.randomUUID();
        var coachId = UUID.randomUUID();

        var startsAt = OffsetDateTime.now().plusDays(1);
        var endsAt = startsAt.plusHours(1);
        var notes = "";

        var req = new CreateAppointmentRequest();
        req.setCoachId(coachId);
        req.setStartsAt(startsAt);
        req.setEndsAt(endsAt);
        req.setNotes(notes);

        var coach = new CoachEntity();
        coach.setUserId(coachId);

        when(coachRepo.findById(req.getCoachId())).thenReturn(Optional.of(coach));
        when(appointmentRepo.findOverlaps(coachId, startsAt, endsAt))
                .thenReturn(List.of());

        when(appointmentRepo.save(any(AppointmentEntity.class)))
                .thenAnswer(inv ->{
                    AppointmentEntity e = inv.getArgument(0);
                    e.setId(UUID.randomUUID());
                    return e;
                });

        AppointmentView result = service.create(clientId, req);

        verify(bookingGuard).assertUserCanBook(clientId);
        var captor = ArgumentCaptor.forClass(AppointmentEntity.class);
        verify(appointmentRepo).save(captor.capture());
        var saved = captor.getValue();

        assertThat(saved.getCoachId()).isEqualTo(coachId);
        assertThat(saved.getClientId()).isEqualTo(clientId);
        assertThat(saved.getStartsAt()).isEqualTo(startsAt);
        assertThat(saved.getEndsAt()).isEqualTo(endsAt);
        assertThat(saved.getNotes()).isEqualTo(notes);
        assertThat(saved.getStatus()).isEqualTo(AppointmentStatus.scheduled);

        assertThat(result.getCoachId()).isEqualTo(saved.getCoachId());
        assertThat(result.getClientId()).isEqualTo(saved.getClientId());
        assertThat(result.getStartsAt()).isEqualTo(saved.getStartsAt());
        assertThat(result.getEndsAt()).isEqualTo(saved.getEndsAt());
        assertThat(result.getNotes()).isEqualTo(saved.getNotes());
        assertThat(result.getStatus()).isEqualTo(saved.getStatus());
    }




    @Test
    void complete() {
        var coachId = UUID.randomUUID();
        var id = UUID.randomUUID();

        var appt = new AppointmentEntity();
        appt.setCoachId(coachId);
        appt.setId(id);
        when(appointmentRepo.findById(id)).thenReturn(Optional.of(appt));
        when(appointmentRepo.save(appt)).thenReturn(appt);
        service.complete(coachId, id);
    }

}