package com.jihedapps.issuetracker.repository;

import com.jihedapps.issuetracker.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {
}
