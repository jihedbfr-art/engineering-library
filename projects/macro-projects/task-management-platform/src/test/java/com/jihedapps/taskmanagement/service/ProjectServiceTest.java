package com.jihedapps.taskmanagement.service;

import com.jihedapps.taskmanagement.entity.Project;
import com.jihedapps.taskmanagement.entity.Role;
import com.jihedapps.taskmanagement.entity.User;
import com.jihedapps.taskmanagement.exception.ForbiddenOperationException;
import com.jihedapps.taskmanagement.repository.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private UserService userService;

    private ProjectService projectService;

    private User admin;
    private User member;

    @BeforeEach
    void setUp() throws Exception {
        projectService = new ProjectService(projectRepository, userService);
        admin = new User("admin", "Admin", Role.ADMIN);
        setId(admin, 1L);
        member = new User("member", "Member", Role.MEMBER);
        setId(member, 2L);
    }

    private static void setId(Object entity, Long id) throws Exception {
        Field field = entity.getClass().getDeclaredField("id");
        field.setAccessible(true);
        field.set(entity, id);
    }

    @Test
    void adminPeutCreerUnProjet() {
        when(userService.requireById(admin.getId())).thenReturn(admin);
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Project project = projectService.createProject(admin.getId(), "Projet X", "description");

        assertEquals("Projet X", project.getName());
        assertEquals(admin, project.getCreatedBy());
    }

    @Test
    void memberNePeutPasCreerUnProjet() {
        when(userService.requireById(member.getId())).thenReturn(member);

        assertThrows(ForbiddenOperationException.class,
                () -> projectService.createProject(member.getId(), "Projet X", "description"));
    }
}
