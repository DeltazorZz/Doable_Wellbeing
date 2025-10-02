package com.dw.doablewellbeingbackend.business.impl.userUseCaseImpl;

import com.dw.doablewellbeingbackend.business.userUseCases.GetUsersUseCase;
import com.dw.doablewellbeingbackend.domain.user.User;
import com.dw.doablewellbeingbackend.presistence.impl.UserRepository;
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
