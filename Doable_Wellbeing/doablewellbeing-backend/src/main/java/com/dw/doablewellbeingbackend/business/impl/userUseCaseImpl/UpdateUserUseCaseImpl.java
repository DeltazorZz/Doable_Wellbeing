package com.dw.doablewellbeingbackend.business.impl.userUseCaseImpl;

import com.dw.doablewellbeingbackend.business.userUseCases.UpdateUserUseCase;
import com.dw.doablewellbeingbackend.common.exception.NotFoundException;
import com.dw.doablewellbeingbackend.domain.user.UpdateUserRequest;
import com.dw.doablewellbeingbackend.presistence.impl.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UpdateUserUseCaseImpl implements UpdateUserUseCase {

    private final UserRepository repo;

    @Override
    @Transactional
    public void update(UUID id, UpdateUserRequest r) {

        var e = repo.findById(id).orElseThrow(() -> new NotFoundException("User not found"));

        if (r.getTitle() != null) e.setTitle(r.getTitle());
        if (r.getFirstName() != null) e.setFirstName(r.getFirstName());
        if (r.getLastName() != null) e.setLastName(r.getLastName());
        if (r.getDateOfBirth() != null) e.setDateOfBirth(r.getDateOfBirth());
        if (r.getGenderIdentity() != null) e.setGenderIdentity(r.getGenderIdentity());
        if (r.getIsGenderSameAsAssignedAtBirth() != null) e.setIsGenderSameAsAssignedAtBirth(r.getIsGenderSameAsAssignedAtBirth());
        if (r.getNhsNumber() != null) e.setNhsNumber(r.getNhsNumber());
        if (r.getIsActive() != null) e.setActive(r.getIsActive());

        repo.save(e);

    }
}
