package com.dw.backend.doablewellbeingbackend.business.appointment;

import com.dw.backend.doablewellbeingbackend.common.exception.ForbiddenException;
import com.dw.backend.doablewellbeingbackend.common.exception.NotFoundException;
import com.dw.backend.doablewellbeingbackend.domain.enums.AppointmentStatus;
import com.dw.backend.doablewellbeingbackend.presistence.entity.AppointmentEntity;
import com.dw.backend.doablewellbeingbackend.presistence.impl.AppointmentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

        var appt = new AppointmentEntity();
        appt.setCoachId(coachId);
        appt.setId(id);

        when(appointmentRepo.findById(id)).thenReturn(Optional.of(appt));
        assertThrows(ForbiddenException.class, () -> service.coachCancel(coachId, id));

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