package com.dw.doablewellbeingbackend.business;

import com.dw.doablewellbeingbackend.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GetUsersUseCase {

    Page<User> getPage(Pageable pageable);

}
