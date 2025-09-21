package com.dw.doablewellbeingbackend.controller;

import com.dw.doablewellbeingbackend.business.*;
import com.dw.doablewellbeingbackend.domain.CreateUserRequest;
import com.dw.doablewellbeingbackend.domain.CreateUserResponse;
import com.dw.doablewellbeingbackend.domain.UpdateUserRequest;
import com.dw.doablewellbeingbackend.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final CreateUserUseCase createUserUseCase;
    private final GetUserUseCase getUserUseCase;
    private final GetUsersUseCase getUsersUseCase;
    private final DeleteUserUseCase deleteUserUseCase;
    private final UpdateUserUseCase updateUserUseCase;


    @PostMapping
    public CreateUserResponse createUser(@Validated @RequestBody CreateUserRequest req){
        return createUserUseCase.createUser(req);
    }


    @GetMapping("/{id}")
    public User get(@PathVariable UUID id){
        return getUserUseCase.getById(id);
    }


    @GetMapping
    public Page<User> list(@RequestParam(defaultValue = "0")int page, @RequestParam(defaultValue = "20")int size){
        return getUsersUseCase.getPage(PageRequest.of(page, size));
    }


    @PutMapping("/{id}")
    public void update(@PathVariable UUID id, @RequestBody UpdateUserRequest req){
        updateUserUseCase.update(id, req);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable UUID id){
        deleteUserUseCase.delete(id);
    }



















}
