package com.dw.doablewellbeingbackend.business.impl.userUseCaseImpl;

import com.dw.doablewellbeingbackend.domain.user.User;
import com.dw.doablewellbeingbackend.presistence.entity.UserEntity;

final class UserMapper {
    static User toDomain(UserEntity e) {
        return User.builder()
                .id(e.getId())
                .email(e.getEmail())
                .title(e.getTitle())
                .firstName(e.getFirstName())
                .lastName(e.getLastName())
                .dateOfBirth(e.getDateOfBirth())
                .genderIdentity(e.getGenderIdentity())
                .isGenderSameAsAssignedAtBirth(e.getIsGenderSameAsAssignedAtBirth())
                .nhsNumber(e.getNhsNumber())
                .isActive(e.isActive())
                .build();
    }

}
