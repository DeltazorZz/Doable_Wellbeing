package com.dw.doablewellbeingbackend.business.impl.userUseCaseImpl;

import com.dw.doablewellbeingbackend.business.userUseCases.CreateUserUseCase;
import com.dw.doablewellbeingbackend.common.exception.ConflictException;
import com.dw.doablewellbeingbackend.domain.user.CreateUserRequest;
import com.dw.doablewellbeingbackend.domain.user.CreateUserResponse;
import com.dw.doablewellbeingbackend.presistence.impl.UserRepository;
import com.dw.doablewellbeingbackend.presistence.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class CreateUserUseCaseImpl implements CreateUserUseCase {
    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;

    @Override
    @Transactional
    public CreateUserResponse createUser(CreateUserRequest r) {
        if (userRepository.existsByEmail(r.getEmail()))
            throw new ConflictException("Email already exists");

        var entity = UserEntity.builder()
                .email(r.getEmail())
                .passwordHash(passwordHasher.hash(r.getPassword()))
                .title(r.getTitle())
                .firstName(r.getFirstName())
                .lastName(r.getLastName())
                .dateOfBirth(r.getDateOfBirth())
                .genderIdentity(r.getGenderIdentity())
                .isGenderSameAsAssignedAtBirth(r.getIsGenderSameAsAssignedAtBirth())
                .nhsNumber(r.getNhsNumber())
                .build();

        entity = userRepository.save(entity);
        return CreateUserResponse.builder().id(entity.getId()).build();
    }
}
