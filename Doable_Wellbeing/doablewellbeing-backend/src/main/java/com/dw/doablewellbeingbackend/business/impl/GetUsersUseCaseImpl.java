package com.dw.doablewellbeingbackend.business.impl;

import com.dw.doablewellbeingbackend.business.GetUsersUseCase;
import com.dw.doablewellbeingbackend.domain.User;
import com.dw.doablewellbeingbackend.presistence.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetUsersUseCaseImpl implements GetUsersUseCase {

    private final UserRepository repo;

    @Override
    public Page<User> getPage(Pageable pageable){
        return repo.findAllByIsActiveTrue(pageable).map(UserMapper::toDomain);
    }
}
