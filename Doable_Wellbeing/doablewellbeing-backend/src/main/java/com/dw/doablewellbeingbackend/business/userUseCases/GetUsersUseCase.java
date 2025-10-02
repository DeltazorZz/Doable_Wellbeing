package com.dw.doablewellbeingbackend.business.userUseCases;

import com.dw.doablewellbeingbackend.domain.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GetUsersUseCase {

    Page<User> getPage(Pageable pageable);

}
