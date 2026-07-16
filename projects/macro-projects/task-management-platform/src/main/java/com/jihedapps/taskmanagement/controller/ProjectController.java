package com.jihedapps.taskmanagement.controller;

import com.jihedapps.taskmanagement.dto.ProjectDtos.CreateProjectRequest;
import com.jihedapps.taskmanagement.dto.ProjectDtos.ProjectResponse;
import com.jihedapps.taskmanagement.entity.Project;
import com.jihedapps.taskmanagement.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * L'identite de l'appelant est transmise via l'en-tete X-User-Id.
 * Simplification assumee : pas de session/JWT dans ce projet de demonstration,
 * voir README (limitations connues).
 */
@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectResponse createProject(@RequestHeader("X-User-Id") Long requesterId,
                                          @Valid @RequestBody CreateProjectRequest request) {
        Project project = projectService.createProject(requesterId, request.name(), request.description());
        return ProjectResponse.from(project);
    }

    @GetMapping
    public List<ProjectResponse> listProjects() {
        return projectService.listAll().stream().map(ProjectResponse::from).toList();
    }

    @GetMapping("/{id}")
    public ProjectResponse getProject(@PathVariable Long id) {
        return ProjectResponse.from(projectService.requireById(id));
    }
}
