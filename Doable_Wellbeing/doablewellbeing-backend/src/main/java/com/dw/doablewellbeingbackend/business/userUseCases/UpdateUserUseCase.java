package com.dw.doablewellbeingbackend.business.userUseCases;

import com.dw.doablewellbeingbackend.domain.user.UpdateUserRequest;

import java.util.UUID;

public interface UpdateUserUseCase {

    void update(UUID id, UpdateUserRequest req);

}
