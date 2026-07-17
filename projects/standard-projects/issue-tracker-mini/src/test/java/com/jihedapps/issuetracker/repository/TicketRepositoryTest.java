package com.jihedapps.issuetracker.repository;

import com.jihedapps.issuetracker.entity.Project;
import com.jihedapps.issuetracker.entity.Ticket;
import com.jihedapps.issuetracker.entity.TicketPriority;
import com.jihedapps.issuetracker.entity.TicketStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * These derived-query method names look right at compile time but Spring Data only actually
 * validates them against the entity model at startup - a typo here fails at runtime, not at
 * compile time. Worth a real H2-backed check rather than trusting the method signatures.
 */
@DataJpaTest
class TicketRepositoryTest {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private ProjectRepository projectRepository;

    private Project projectA;
    private Project projectB;

    @BeforeEach
    void setUp() {
        projectA = projectRepository.save(new Project("Project A", "first"));
        projectB = projectRepository.save(new Project("Project B", "second"));

        ticketRepository.save(new Ticket("A-todo-high", "d", TicketStatus.TODO, TicketPriority.HIGH, projectA));
        ticketRepository.save(new Ticket("A-done-low", "d", TicketStatus.DONE, TicketPriority.LOW, projectA));
        ticketRepository.save(new Ticket("B-todo-high", "d", TicketStatus.TODO, TicketPriority.HIGH, projectB));
    }

    @Test
    void findByProjectIdReturnsOnlyThatProjectsTickets() {
        List<Ticket> result = ticketRepository.findByProjectId(projectA.getId());

        assertThat(result).extracting(Ticket::getTitle)
                .containsExactlyInAnyOrder("A-todo-high", "A-done-low");
    }

    @Test
    void findByProjectIdAndStatus() {
        List<Ticket> result = ticketRepository.findByProjectIdAndStatus(projectA.getId(), TicketStatus.TODO);

        assertThat(result).extracting(Ticket::getTitle).containsExactly("A-todo-high");
    }

    @Test
    void findByProjectIdAndPriority() {
        List<Ticket> result = ticketRepository.findByProjectIdAndPriority(projectA.getId(), TicketPriority.LOW);

        assertThat(result).extracting(Ticket::getTitle).containsExactly("A-done-low");
    }

    @Test
    void findByProjectIdAndStatusAndPriority() {
        List<Ticket> result = ticketRepository.findByProjectIdAndStatusAndPriority(
                projectA.getId(), TicketStatus.TODO, TicketPriority.HIGH);

        assertThat(result).extracting(Ticket::getTitle).containsExactly("A-todo-high");
    }

    @Test
    void findByStatusCutsAcrossProjects() {
        List<Ticket> result = ticketRepository.findByStatus(TicketStatus.TODO);

        assertThat(result).extracting(Ticket::getTitle)
                .containsExactlyInAnyOrder("A-todo-high", "B-todo-high");
    }

    @Test
    void findByPriorityCutsAcrossProjects() {
        List<Ticket> result = ticketRepository.findByPriority(TicketPriority.HIGH);

        assertThat(result).extracting(Ticket::getTitle)
                .containsExactlyInAnyOrder("A-todo-high", "B-todo-high");
    }
}
