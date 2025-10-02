package com.dw.doablewellbeingbackend.business.impl.userUseCaseImpl;

import com.dw.doablewellbeingbackend.business.userUseCases.DeleteUserUseCase;
import com.dw.doablewellbeingbackend.presistence.impl.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeleteUserUseCaseImpl implements DeleteUserUseCase {

    private final UserRepository repo;

    @Override
    @Transactional
    public void delete(UUID id){
        repo.findById(id).ifPresent(repo::delete);
    }
}

