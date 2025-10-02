package com.dw.doablewellbeingbackend.business.impl.userUseCaseImpl;

import com.dw.doablewellbeingbackend.business.userUseCases.GetUserUseCase;
import com.dw.doablewellbeingbackend.common.exception.NotFoundException;
import com.dw.doablewellbeingbackend.domain.user.User;
import com.dw.doablewellbeingbackend.presistence.impl.UserRepository;
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
