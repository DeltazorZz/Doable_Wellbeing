package com.dw.doablewellbeingbackend.business;

import com.dw.doablewellbeingbackend.domain.CreateUserRequest;
import com.dw.doablewellbeingbackend.domain.CreateUserResponse;

public interface CreateUserUseCase {

    CreateUserResponse createUser(CreateUserRequest request);

}
