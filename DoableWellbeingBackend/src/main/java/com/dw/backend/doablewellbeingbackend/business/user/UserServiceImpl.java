package com.dw.backend.doablewellbeingbackend.business.user;

import com.dw.backend.doablewellbeingbackend.common.exception.NotFoundException;
import com.dw.backend.doablewellbeingbackend.domain.user.*;
import com.dw.backend.doablewellbeingbackend.presistence.entity.RoleEntity;
import com.dw.backend.doablewellbeingbackend.presistence.entity.UserEntity;
import com.dw.backend.doablewellbeingbackend.presistence.impl.RoleRepository;
import com.dw.backend.doablewellbeingbackend.presistence.impl.UserRepository;
import com.dw.backend.doablewellbeingbackend.business.user.AppUserImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;



    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmailIgnoreCase(email);
    }

    @Override
    public Optional<AppUser> findByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email).map(this::toAppUser);
    }

    @Override
    public AppUser getRequired(UUID id) {
        return toAppUser(
                userRepository.findById(id)
                        .orElseThrow(() -> new NotFoundException("User not found: " + id))
        );
    }

    // ----- Create User -----

    @Override
    public AppUser createUser(String email, String passwordHash, byte[] passwordSalt, String firstName, String lastName) {
        Objects.requireNonNull(passwordHash, "passwordHash");
        Objects.requireNonNull(passwordSalt, "passwordSalt");

        UserEntity u = new UserEntity();
        u.setEmail(email);
        u.setPasswordHash(passwordHash);

        u.setPasswordSalt(passwordSalt.clone());
        u.setFirstName(firstName == null ? "" : firstName.trim());
        u.setLastName(lastName == null ? "" : lastName.trim());

        if (u.getRoles() == null) {
            u.setRoles(new HashSet<>());
        }
        RoleEntity userRole = roleRepository.findByName("user")
                .orElseThrow(() -> new IllegalStateException("Missing ROLE seed: USER"));
        u.getRoles().add(userRole);

        UserEntity saved = userRepository.save(u);
        return toAppUser(saved);
    }

    // ----- Get User(s) -----

    @Override
    public User getById(UUID id) {
        UserEntity e = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
        return UserMapper.toDomain(e);
    }

    @Override
    public Page<User> getAllActive(Pageable pageable) {
        return userRepository.findAllByIsActiveTrue(pageable).map(UserMapper::toDomain);
    }

    @Override
    public Page<User> getAll(Pageable pageable) {
        return userRepository.findAll(pageable).map(UserMapper::toDomain);
    }

    // ----- Update User -----

    @Override
    public void update(UUID id, UpdateUserRequest r) {
        UserEntity e = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
        if (r.getFirstName() != null) e.setFirstName(r.getFirstName());
        if (r.getLastName() != null) e.setLastName(r.getLastName());
        if (r.getDateOfBirth() != null) e.setDateOfBirth(r.getDateOfBirth());
        if (r.getNhsNumber() != null) e.setNhsNumber(r.getNhsNumber());
        if (r.getIsActive() != null) e.setActive(r.getIsActive());

        userRepository.save(e);
    }

    // ----- Delete User -----

    @Override
    public void delete(UUID id) {
        userRepository.findById(id).ifPresent(userRepository::delete);
    }

    // ----- Helpers -----

    private AppUser toAppUser(UserEntity e) {
        byte[] salt = e.getPasswordSalt() == null ? null : e.getPasswordSalt().clone();
        List<String> roles = e.getRoles() == null
                ? List.of()
                : e.getRoles().stream().map(RoleEntity::getName).toList();

        return new AppUserImpl(
                e.getId(),
                e.getEmail(),
                e.getFirstName(),
                e.getLastName(),
                e.getPasswordHash(),
                salt,
                e.isActive(),
                e.isDeleted(),
                roles
        );
    }


}
