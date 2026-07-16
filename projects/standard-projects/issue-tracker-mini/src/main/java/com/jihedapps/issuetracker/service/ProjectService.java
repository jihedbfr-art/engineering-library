package com.jihedapps.issuetracker.service;

import com.jihedapps.issuetracker.entity.Project;
import com.jihedapps.issuetracker.exception.ResourceNotFoundException;
import com.jihedapps.issuetracker.repository.ProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ProjectService {

    private final ProjectRepository projectRepository;

    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    @Transactional(readOnly = true)
    public List<Project> findAll() {
        return projectRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Project findById(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Projet introuvable : " + id));
    }

    public Project create(Project project) {
        return projectRepository.save(project);
    }

    public Project update(Long id, Project payload) {
        Project existing = findById(id);
        existing.setName(payload.getName());
        existing.setDescription(payload.getDescription());
        return projectRepository.save(existing);
    }

    public void delete(Long id) {
        Project existing = findById(id);
        projectRepository.delete(existing);
    }
}
