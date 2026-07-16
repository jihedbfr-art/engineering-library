package com.jihedapps.taskmanagement.dto;

import com.jihedapps.taskmanagement.entity.Role;
import com.jihedapps.taskmanagement.entity.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class UserDtos {

    public record CreateUserRequest(
            @NotBlank String username,
            @NotBlank String displayName,
            @NotNull Role role) {
    }

    public record UserResponse(Long id, String username, String displayName, Role role) {
        public static UserResponse from(User user) {
            return new UserResponse(user.getId(), user.getUsername(), user.getDisplayName(), user.getRole());
        }
    }
}
