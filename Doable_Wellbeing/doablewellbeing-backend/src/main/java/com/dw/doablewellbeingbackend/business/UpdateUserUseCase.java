package com.dw.doablewellbeingbackend.business;

import com.dw.doablewellbeingbackend.domain.UpdateUserRequest;

import java.util.UUID;

public interface UpdateUserUseCase {

    void update(UUID id, UpdateUserRequest req);

}
