package com.jihedapps.taskmanagement.dto;

import com.jihedapps.taskmanagement.entity.Task;
import com.jihedapps.taskmanagement.entity.TaskPriority;
import com.jihedapps.taskmanagement.entity.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class TaskDtos {

    public record CreateTaskRequest(
            @NotBlank String title,
            String description,
            @NotNull Long projectId,
            Long assigneeId,
            TaskPriority priority,
            @NotNull LocalDateTime deadline) {
    }

    public record UpdateTaskRequest(
            String title,
            String description,
            Long assigneeId,
            TaskStatus status,
            TaskPriority priority,
            LocalDateTime deadline) {
    }

    public record TaskResponse(
            Long id,
            String title,
            String description,
            Long projectId,
            String projectName,
            Long assigneeId,
            String assigneeUsername,
            TaskStatus status,
            TaskPriority priority,
            LocalDateTime deadline,
            boolean overdue) {

        public static TaskResponse from(Task task, LocalDateTime now) {
            return new TaskResponse(
                    task.getId(),
                    task.getTitle(),
                    task.getDescription(),
                    task.getProject().getId(),
                    task.getProject().getName(),
                    task.getAssignee() != null ? task.getAssignee().getId() : null,
                    task.getAssignee() != null ? task.getAssignee().getUsername() : null,
                    task.getStatus(),
                    task.getPriority(),
                    task.getDeadline(),
                    task.isOverdue(now));
        }
    }
}
