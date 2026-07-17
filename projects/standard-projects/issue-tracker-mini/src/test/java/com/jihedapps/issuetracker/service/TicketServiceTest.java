package com.jihedapps.issuetracker.service;

import com.jihedapps.issuetracker.entity.Project;
import com.jihedapps.issuetracker.entity.Ticket;
import com.jihedapps.issuetracker.entity.TicketPriority;
import com.jihedapps.issuetracker.entity.TicketStatus;
import com.jihedapps.issuetracker.exception.ResourceNotFoundException;
import com.jihedapps.issuetracker.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private ProjectService projectService;

    private TicketService ticketService;

    @BeforeEach
    void setUp() {
        ticketService = new TicketService(ticketRepository, projectService);
    }

    private Ticket ticket(Long id, Project project, TicketStatus status, TicketPriority priority) {
        Ticket ticket = new Ticket("title", "desc", status, priority, project);
        ticket.setId(id);
        return ticket;
    }

    @Test
    void findByIdThrowsWhenMissing() {
        when(ticketRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ticketService.findById(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("1");
    }

    @Test
    void createAssignsProjectAndClearsIncomingId() {
        Project project = new Project("P", "d");
        project.setId(10L);
        when(projectService.findById(10L)).thenReturn(project);
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Ticket payload = new Ticket("Bug", "desc", TicketStatus.TODO, TicketPriority.HIGH, null);
        payload.setId(999L);

        Ticket result = ticketService.create(10L, payload);

        assertThat(result.getId()).isNull();
        assertThat(result.getProject()).isSameAs(project);
    }

    @Test
    void updateOverwritesFieldsButKeepsProjectAndId() {
        Project project = new Project("P", "d");
        Ticket existing = ticket(1L, project, TicketStatus.TODO, TicketPriority.LOW);
        Ticket payload = new Ticket("New title", "New desc", TicketStatus.DONE, TicketPriority.CRITICAL, null);
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Ticket result = ticketService.update(1L, payload);

        assertThat(result.getTitle()).isEqualTo("New title");
        assertThat(result.getDescription()).isEqualTo("New desc");
        assertThat(result.getStatus()).isEqualTo(TicketStatus.DONE);
        assertThat(result.getPriority()).isEqualTo(TicketPriority.CRITICAL);
        assertThat(result.getProject()).isSameAs(project);
    }

    @Test
    void updateStatusOnlyChangesStatus() {
        Ticket existing = ticket(2L, new Project("P", "d"), TicketStatus.TODO, TicketPriority.MEDIUM);
        when(ticketRepository.findById(2L)).thenReturn(Optional.of(existing));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Ticket result = ticketService.updateStatus(2L, TicketStatus.IN_PROGRESS);

        assertThat(result.getStatus()).isEqualTo(TicketStatus.IN_PROGRESS);
        assertThat(result.getPriority()).isEqualTo(TicketPriority.MEDIUM);
    }

    @Test
    void deleteRemovesExistingTicket() {
        Ticket existing = ticket(3L, new Project("P", "d"), TicketStatus.TODO, TicketPriority.LOW);
        when(ticketRepository.findById(3L)).thenReturn(Optional.of(existing));

        ticketService.delete(3L);

        verify(ticketRepository).delete(existing);
    }

    @Test
    void searchWithProjectStatusAndPriorityUsesTheMostSpecificQuery() {
        ticketService.search(1L, TicketStatus.TODO, TicketPriority.HIGH);

        verify(ticketRepository).findByProjectIdAndStatusAndPriority(1L, TicketStatus.TODO, TicketPriority.HIGH);
        verify(ticketRepository, never()).findAll();
    }

    @Test
    void searchWithProjectAndStatusOnly() {
        ticketService.search(1L, TicketStatus.TODO, null);

        verify(ticketRepository).findByProjectIdAndStatus(1L, TicketStatus.TODO);
    }

    @Test
    void searchWithProjectAndPriorityOnly() {
        ticketService.search(1L, null, TicketPriority.HIGH);

        verify(ticketRepository).findByProjectIdAndPriority(1L, TicketPriority.HIGH);
    }

    @Test
    void searchWithProjectOnly() {
        ticketService.search(1L, null, null);

        verify(ticketRepository).findByProjectId(1L);
    }

    @Test
    void searchWithStatusOnly() {
        ticketService.search(null, TicketStatus.DONE, null);

        verify(ticketRepository).findByStatus(TicketStatus.DONE);
    }

    @Test
    void searchWithPriorityOnly() {
        ticketService.search(null, null, TicketPriority.CRITICAL);

        verify(ticketRepository).findByPriority(TicketPriority.CRITICAL);
    }

    @Test
    void searchWithNoFiltersReturnsEverything() {
        when(ticketRepository.findAll()).thenReturn(List.of());

        List<Ticket> result = ticketService.search(null, null, null);

        assertThat(result).isEmpty();
        verify(ticketRepository).findAll();
    }
}
