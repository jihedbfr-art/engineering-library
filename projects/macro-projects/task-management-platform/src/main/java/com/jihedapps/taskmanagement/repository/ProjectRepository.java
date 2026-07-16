package com.jihedapps.taskmanagement.repository;

import com.jihedapps.taskmanagement.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {
}
