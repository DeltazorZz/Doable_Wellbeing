package com.dw.doablewellbeingbackend.business.userUseCases;

import com.dw.doablewellbeingbackend.domain.user.User;

import java.util.UUID;

public interface GetUserUseCase {

    User getById(UUID id);
}
