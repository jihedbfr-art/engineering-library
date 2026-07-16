package com.jihedapps.taskmanagement.controller;

import com.jihedapps.taskmanagement.dto.UserDtos.CreateUserRequest;
import com.jihedapps.taskmanagement.dto.UserDtos.UserResponse;
import com.jihedapps.taskmanagement.entity.User;
import com.jihedapps.taskmanagement.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse createUser(@Valid @RequestBody CreateUserRequest request) {
        User user = userService.createUser(request.username(), request.displayName(), request.role());
        return UserResponse.from(user);
    }

    @GetMapping
    public List<UserResponse> listUsers() {
        return userService.listAll().stream().map(UserResponse::from).toList();
    }

    @GetMapping("/{id}")
    public UserResponse getUser(@PathVariable Long id) {
        return UserResponse.from(userService.requireById(id));
    }
}
