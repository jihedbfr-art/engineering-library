package com.jihedapps.issuetracker.service;

import com.jihedapps.issuetracker.entity.Project;
import com.jihedapps.issuetracker.exception.ResourceNotFoundException;
import com.jihedapps.issuetracker.repository.ProjectRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    private ProjectService projectService;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        projectService = new ProjectService(projectRepository);
    }

    @Test
    void findByIdReturnsProjectWhenPresent() {
        Project project = new Project("Foreign Trade Platform", "single window for customs");
        project.setId(1L);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        Project found = projectService.findById(1L);

        assertThat(found.getName()).isEqualTo("Foreign Trade Platform");
    }

    @Test
    void findByIdThrowsWhenMissing() {
        when(projectRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void findAllDelegatesToRepository() {
        Project project = new Project("P1", "d1");
        when(projectRepository.findAll()).thenReturn(List.of(project));

        List<Project> all = projectService.findAll();

        assertThat(all).containsExactly(project);
    }

    @Test
    void createSavesAndReturnsProject() {
        Project toCreate = new Project("New project", "desc");
        Project saved = new Project("New project", "desc");
        saved.setId(5L);
        when(projectRepository.save(toCreate)).thenReturn(saved);

        Project result = projectService.create(toCreate);

        assertThat(result.getId()).isEqualTo(5L);
    }

    @Test
    void updateOverwritesNameAndDescriptionThenSaves() {
        Project existing = new Project("Old name", "Old description");
        existing.setId(3L);
        Project payload = new Project("New name", "New description");
        when(projectRepository.findById(3L)).thenReturn(Optional.of(existing));
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Project result = projectService.update(3L, payload);

        assertThat(result.getName()).isEqualTo("New name");
        assertThat(result.getDescription()).isEqualTo("New description");
    }

    @Test
    void updateThrowsWhenProjectMissing() {
        when(projectRepository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.update(42L, new Project("x", "y")))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteRemovesExistingProject() {
        Project existing = new Project("To delete", "desc");
        existing.setId(7L);
        when(projectRepository.findById(7L)).thenReturn(Optional.of(existing));

        projectService.delete(7L);

        verify(projectRepository).delete(existing);
    }

    @Test
    void deleteThrowsWhenProjectMissing() {
        when(projectRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.delete(404L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
