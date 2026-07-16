package com.jihedapps.taskmanagement.controller;

import com.jihedapps.taskmanagement.dto.TaskDtos.CreateTaskRequest;
import com.jihedapps.taskmanagement.dto.TaskDtos.TaskResponse;
import com.jihedapps.taskmanagement.dto.TaskDtos.UpdateTaskRequest;
import com.jihedapps.taskmanagement.entity.Task;
import com.jihedapps.taskmanagement.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;
    private final Clock clock;

    public TaskController(TaskService taskService, Clock clock) {
        this.taskService = taskService;
        this.clock = clock;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse createTask(@RequestHeader("X-User-Id") Long requesterId,
                                    @Valid @RequestBody CreateTaskRequest request) {
        Task task = taskService.createTask(requesterId, request.projectId(), request.title(),
                request.description(), request.assigneeId(), request.priority(), request.deadline());
        return TaskResponse.from(task, LocalDateTime.now(clock));
    }

    @GetMapping
    public List<TaskResponse> listTasks(@RequestParam(required = false) Long projectId) {
        LocalDateTime now = LocalDateTime.now(clock);
        List<Task> tasks = projectId != null ? taskService.listByProject(projectId) : taskService.listAll();
        return tasks.stream().map(t -> TaskResponse.from(t, now)).toList();
    }

    @GetMapping("/overdue")
    public List<TaskResponse> listOverdueTasks() {
        LocalDateTime now = LocalDateTime.now(clock);
        return taskService.findOverdueTasks().stream().map(t -> TaskResponse.from(t, now)).toList();
    }

    @GetMapping("/{id}")
    public TaskResponse getTask(@PathVariable Long id) {
        return TaskResponse.from(taskService.requireById(id), LocalDateTime.now(clock));
    }

    @PutMapping("/{id}")
    public TaskResponse updateTask(@RequestHeader("X-User-Id") Long requesterId,
                                    @PathVariable Long id,
                                    @RequestBody UpdateTaskRequest request) {
        Task task = taskService.updateTask(requesterId, id, request.title(), request.description(),
                request.assigneeId(), request.status(), request.priority(), request.deadline());
        return TaskResponse.from(task, LocalDateTime.now(clock));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTask(@RequestHeader("X-User-Id") Long requesterId, @PathVariable Long id) {
        taskService.deleteTask(requesterId, id);
    }
}
