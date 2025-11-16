package com.dw.backend.doablewellbeingbackend.business.user;

import com.dw.backend.doablewellbeingbackend.domain.user.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface UserService {


    // Create User
    //CreateUserResponse createUser(CreateUserRequest request);
    AppUser createUser(String email, String passwordHash, byte[] passwordSalt, String firstName, String lastName);

    // Get User
    User getById(UUID id);

    // Get Users

    Page<User> getAllActive(Pageable pageable);
    Page<User> getAll(Pageable pageable);


    // Update User

    void update(UUID id, UpdateUserRequest req);

    // Delete User

    void delete(UUID id);

    // Validation
    boolean existsByEmail(String email);
    AppUser getRequired(UUID id);
    Optional<AppUser> findByEmail(String email);

}
