package com.dw.backend.doablewellbeingbackend.controller;

import com.dw.backend.doablewellbeingbackend.business.user.UserService;
import com.dw.backend.doablewellbeingbackend.domain.user.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {


    private final UserService userService;



    @GetMapping("/{id}")
    public User get(@PathVariable UUID id){
        return userService.getById(id);
    }


    @GetMapping("/active")
    public Page<User> listActive(@RequestParam(defaultValue = "0")int page, @RequestParam(defaultValue = "20")int size){
        return userService.getAllActive(PageRequest.of(page, size));
    }

     @GetMapping("/all")
    public Page<User> listAll(@RequestParam(defaultValue = "0")int page, @RequestParam(defaultValue = "20")int size){
        return userService.getAll(PageRequest.of(page, size));
    }

    @PutMapping("/{id}")
    public void update(@PathVariable UUID id, @RequestBody UpdateUserRequest req){
        userService.update(id, req);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable UUID id){
        userService.delete(id);
    }



















}
