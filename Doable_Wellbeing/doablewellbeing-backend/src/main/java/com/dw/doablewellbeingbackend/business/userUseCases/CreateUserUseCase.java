package com.dw.doablewellbeingbackend.business.userUseCases;

import com.dw.doablewellbeingbackend.domain.user.CreateUserRequest;
import com.dw.doablewellbeingbackend.domain.user.CreateUserResponse;

public interface CreateUserUseCase {

    CreateUserResponse createUser(CreateUserRequest request);

}
