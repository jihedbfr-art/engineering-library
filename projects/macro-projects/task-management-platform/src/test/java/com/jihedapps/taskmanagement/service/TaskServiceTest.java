package com.jihedapps.taskmanagement.service;

import com.jihedapps.taskmanagement.entity.*;
import com.jihedapps.taskmanagement.exception.ForbiddenOperationException;
import com.jihedapps.taskmanagement.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Tests de la logique metier critique de TaskService :
 * - regles d'autorisation (ADMIN vs MEMBER assigne)
 * - suppression reservee aux ADMIN
 * - detection des taches en retard
 */
@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;
    @Mock
    private UserService userService;
    @Mock
    private ProjectService projectService;

    private TaskService taskService;

    private static final ZoneId ZONE = ZoneId.of("Europe/Paris");
    private static final LocalDateTime FIXED_NOW = LocalDateTime.of(2026, 7, 16, 12, 0);

    private User admin;
    private User memberOwner;
    private User memberOther;
    private Project project;

    @BeforeEach
    void setUp() throws Exception {
        Clock fixedClock = Clock.fixed(FIXED_NOW.atZone(ZONE).toInstant(), ZONE);
        taskService = new TaskService(taskRepository, userService, projectService, fixedClock);

        admin = new User("admin", "Admin", Role.ADMIN);
        setId(admin, 1L);
        memberOwner = new User("owner", "Owner Member", Role.MEMBER);
        setId(memberOwner, 2L);
        memberOther = new User("other", "Other Member", Role.MEMBER);
        setId(memberOther, 3L);

        project = new Project("Projet Alpha", "desc", admin);
        setId(project, 10L);
    }

    private static void setId(Object entity, Long id) throws Exception {
        Field field = entity.getClass().getDeclaredField("id");
        field.setAccessible(true);
        field.set(entity, id);
    }

    private Task newTask(User assignee, TaskStatus status, LocalDateTime deadline) throws Exception {
        Task task = new Task("Titre", "desc", project, assignee, TaskPriority.MEDIUM, deadline);
        setId(task, 100L);
        Field statusField = Task.class.getDeclaredField("status");
        statusField.setAccessible(true);
        statusField.set(task, status);
        return task;
    }

    @Test
    void adminPeutModifierNimporteQuelleTache() throws Exception {
        Task task = newTask(memberOther, TaskStatus.TODO, FIXED_NOW.plusDays(1));
        assertDoesNotThrow(() -> taskService.checkCanModify(admin, task));
    }

    @Test
    void memberPeutModifierSaProprePropreTacheAssignee() throws Exception {
        Task task = newTask(memberOwner, TaskStatus.TODO, FIXED_NOW.plusDays(1));
        assertDoesNotThrow(() -> taskService.checkCanModify(memberOwner, task));
    }

    @Test
    void memberNePeutPasModifierUneTacheAssigneeAUnAutre() throws Exception {
        Task task = newTask(memberOwner, TaskStatus.TODO, FIXED_NOW.plusDays(1));
        assertThrows(ForbiddenOperationException.class,
                () -> taskService.checkCanModify(memberOther, task));
    }

    @Test
    void memberNePeutPasModifierUneTacheNonAssignee() throws Exception {
        Task task = newTask(null, TaskStatus.TODO, FIXED_NOW.plusDays(1));
        assertThrows(ForbiddenOperationException.class,
                () -> taskService.checkCanModify(memberOther, task));
    }

    @Test
    void memberNePeutPasSupprimerUneTache() throws Exception {
        Task task = newTask(memberOwner, TaskStatus.TODO, FIXED_NOW.plusDays(1));
        when(userService.requireById(memberOwner.getId())).thenReturn(memberOwner);
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));

        assertThrows(ForbiddenOperationException.class,
                () -> taskService.deleteTask(memberOwner.getId(), task.getId()));
    }

    @Test
    void adminPeutSupprimerUneTache() throws Exception {
        Task task = newTask(memberOwner, TaskStatus.TODO, FIXED_NOW.plusDays(1));
        when(userService.requireById(admin.getId())).thenReturn(admin);
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));

        assertDoesNotThrow(() -> taskService.deleteTask(admin.getId(), task.getId()));
    }

    @Test
    void tacheAvecDeadlineDepasseeEtStatutNonDoneEstEnRetard() throws Exception {
        Task task = newTask(memberOwner, TaskStatus.IN_PROGRESS, FIXED_NOW.minusDays(1));
        assertTrue(task.isOverdue(FIXED_NOW));
    }

    @Test
    void tacheTermineeNestJamaisEnRetardMemeAvecDeadlineDepassee() throws Exception {
        Task task = newTask(memberOwner, TaskStatus.DONE, FIXED_NOW.minusDays(1));
        assertFalse(task.isOverdue(FIXED_NOW));
    }

    @Test
    void tacheAvecDeadlineFutureNestPasEnRetard() throws Exception {
        Task task = newTask(memberOwner, TaskStatus.TODO, FIXED_NOW.plusDays(1));
        assertFalse(task.isOverdue(FIXED_NOW));
    }

    @Test
    void findOverdueTasksDelegueAuRepositoryAvecStatutDoneEtHeureCourante() {
        Task overdue = null;
        try {
            overdue = newTask(memberOwner, TaskStatus.TODO, FIXED_NOW.minusHours(1));
        } catch (Exception e) {
            fail(e);
        }
        when(taskRepository.findByStatusNotAndDeadlineBefore(any(), any())).thenReturn(List.of(overdue));

        List<Task> result = taskService.findOverdueTasks();

        assertEquals(1, result.size());
        assertEquals(overdue.getId(), result.get(0).getId());
    }
}
