package com.dw.doablewellbeingbackend.business.impl;

import com.dw.doablewellbeingbackend.business.GetUserUseCase;
import com.dw.doablewellbeingbackend.common.exception.NotFoundException;
import com.dw.doablewellbeingbackend.domain.User;
import com.dw.doablewellbeingbackend.presistence.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetUserUseCaseImpl implements GetUserUseCase {
    private final UserRepository repo;

    @Override
    public User getById(UUID id){
        var e = repo.findById(id).orElseThrow(() -> new NotFoundException("User not found"));
        return UserMapper.toDomain(e);
    }

}
