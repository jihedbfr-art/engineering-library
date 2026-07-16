package com.jihedapps.taskmanagement.repository;

import com.jihedapps.taskmanagement.entity.Task;
import com.jihedapps.taskmanagement.entity.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByProjectId(Long projectId);

    List<Task> findByAssigneeId(Long assigneeId);

    List<Task> findByStatusNotAndDeadlineBefore(TaskStatus status, LocalDateTime now);
}
