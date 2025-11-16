package com.dw.backend.doablewellbeingbackend.business.user;


import com.dw.backend.doablewellbeingbackend.domain.user.User;
import com.dw.backend.doablewellbeingbackend.presistence.entity.UserEntity;

final class UserMapper {
    static User toDomain(UserEntity e) {
        return User.builder()
                .id(e.getId())
                .email(e.getEmail())
                .firstName(e.getFirstName())
                .lastName(e.getLastName())
                .dateOfBirth(e.getDateOfBirth())
                .nhsNumber(e.getNhsNumber())
                .isActive(e.isActive())
                .build();
    }

}
