package com.dw.doablewellbeingbackend.business;

import com.dw.doablewellbeingbackend.domain.User;

import java.util.UUID;

public interface GetUserUseCase {

    User getById(UUID id);
}
