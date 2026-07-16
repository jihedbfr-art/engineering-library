package com.jihedapps.taskmanagement.service;

import com.jihedapps.taskmanagement.entity.Project;
import com.jihedapps.taskmanagement.entity.User;
import com.jihedapps.taskmanagement.exception.ForbiddenOperationException;
import com.jihedapps.taskmanagement.exception.ResourceNotFoundException;
import com.jihedapps.taskmanagement.repository.ProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserService userService;

    public ProjectService(ProjectRepository projectRepository, UserService userService) {
        this.projectRepository = projectRepository;
        this.userService = userService;
    }

    /**
     * Regle metier : seul un utilisateur avec le role ADMIN peut creer un projet.
     */
    @Transactional
    public Project createProject(Long requesterId, String name, String description) {
        User requester = userService.requireById(requesterId);
        if (!requester.isAdmin()) {
            throw new ForbiddenOperationException("Seul un ADMIN peut creer un projet.");
        }
        Project project = new Project(name, description, requester);
        return projectRepository.save(project);
    }

    @Transactional(readOnly = true)
    public Project requireById(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Projet introuvable : id=" + id));
    }

    @Transactional(readOnly = true)
    public List<Project> listAll() {
        return projectRepository.findAll();
    }
}
