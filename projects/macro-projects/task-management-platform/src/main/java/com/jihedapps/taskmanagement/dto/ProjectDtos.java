package com.jihedapps.taskmanagement.dto;

import com.jihedapps.taskmanagement.entity.Project;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

public class ProjectDtos {

    public record CreateProjectRequest(@NotBlank String name, String description) {
    }

    public record ProjectResponse(Long id, String name, String description, String createdBy, LocalDateTime createdAt) {
        public static ProjectResponse from(Project project) {
            return new ProjectResponse(
                    project.getId(),
                    project.getName(),
                    project.getDescription(),
                    project.getCreatedBy().getUsername(),
                    project.getCreatedAt());
        }
    }
}
