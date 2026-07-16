package com.jihedapps.taskmanagement.service;

import com.jihedapps.taskmanagement.entity.*;
import com.jihedapps.taskmanagement.exception.ForbiddenOperationException;
import com.jihedapps.taskmanagement.exception.ResourceNotFoundException;
import com.jihedapps.taskmanagement.repository.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserService userService;
    private final ProjectService projectService;
    private final Clock clock;

    public TaskService(TaskRepository taskRepository, UserService userService,
                        ProjectService projectService, Clock clock) {
        this.taskRepository = taskRepository;
        this.userService = userService;
        this.projectService = projectService;
        this.clock = clock;
    }

    @Transactional
    public Task createTask(Long requesterId, Long projectId, String title, String description,
                            Long assigneeId, TaskPriority priority, LocalDateTime deadline) {
        // creation ouverte a ADMIN et MEMBER : la restriction metier porte sur la
        // modification et la suppression, pas sur la creation.
        userService.requireById(requesterId);
        Project project = projectService.requireById(projectId);
        User assignee = assigneeId != null ? userService.requireById(assigneeId) : null;
        TaskPriority effectivePriority = priority != null ? priority : TaskPriority.MEDIUM;

        Task task = new Task(title, description, project, assignee, effectivePriority, deadline);
        return taskRepository.save(task);
    }

    @Transactional(readOnly = true)
    public Task requireById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tache introuvable : id=" + id));
    }

    @Transactional(readOnly = true)
    public List<Task> listAll() {
        return taskRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Task> listByProject(Long projectId) {
        return taskRepository.findByProjectId(projectId);
    }

    /**
     * Regle metier d'autorisation :
     * - un ADMIN peut modifier n'importe quelle tache
     * - un MEMBER ne peut modifier qu'une tache qui lui est assignee
     */
    public void checkCanModify(User requester, Task task) {
        if (requester.isAdmin()) {
            return;
        }
        boolean assignedToRequester = task.getAssignee() != null
                && task.getAssignee().getId().equals(requester.getId());
        if (!assignedToRequester) {
            throw new ForbiddenOperationException(
                    "Un MEMBER ne peut modifier que les taches qui lui sont assignees.");
        }
    }

    @Transactional
    public Task updateTask(Long requesterId, Long taskId, String title, String description,
                            Long assigneeId, TaskStatus status, TaskPriority priority, LocalDateTime deadline) {
        User requester = userService.requireById(requesterId);
        Task task = requireById(taskId);
        checkCanModify(requester, task);

        if (title != null && !title.isBlank()) {
            task.setTitle(title);
        }
        if (description != null) {
            task.setDescription(description);
        }
        if (assigneeId != null) {
            task.setAssignee(userService.requireById(assigneeId));
        }
        if (status != null) {
            task.setStatus(status);
        }
        if (priority != null) {
            task.setPriority(priority);
        }
        if (deadline != null) {
            task.setDeadline(deadline);
        }
        return task;
    }

    /**
     * Regle metier : seul un ADMIN peut supprimer une tache.
     */
    @Transactional
    public void deleteTask(Long requesterId, Long taskId) {
        User requester = userService.requireById(requesterId);
        Task task = requireById(taskId);
        if (!requester.isAdmin()) {
            throw new ForbiddenOperationException("Seul un ADMIN peut supprimer une tache.");
        }
        taskRepository.delete(task);
    }

    /**
     * Detection des taches en retard : deadline depassee et statut != DONE.
     */
    @Transactional(readOnly = true)
    public List<Task> findOverdueTasks() {
        LocalDateTime now = LocalDateTime.now(clock);
        return taskRepository.findByStatusNotAndDeadlineBefore(TaskStatus.DONE, now);
    }
}
