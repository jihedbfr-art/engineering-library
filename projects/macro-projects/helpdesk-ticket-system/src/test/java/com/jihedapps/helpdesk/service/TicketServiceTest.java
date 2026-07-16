package com.jihedapps.helpdesk.service;

import com.jihedapps.helpdesk.dto.ResolutionReport;
import com.jihedapps.helpdesk.entity.*;
import com.jihedapps.helpdesk.exception.ForbiddenOperationException;
import com.jihedapps.helpdesk.exception.InvalidStatusTransitionException;
import com.jihedapps.helpdesk.repository.TicketHistoryRepository;
import com.jihedapps.helpdesk.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Tests de la logique metier critique de TicketService :
 * - workflow de statuts strict et sequentiel (pas de saut, pas de retour)
 * - reassignation reservee aux SUPERVISOR
 * - calcul du temps moyen de resolution par agent
 */
@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;
    @Mock
    private TicketHistoryRepository ticketHistoryRepository;
    @Mock
    private AgentService agentService;

    private TicketService ticketService;

    private static final ZoneId ZONE = ZoneId.of("Europe/Paris");
    private static final LocalDateTime FIXED_NOW = LocalDateTime.of(2026, 7, 16, 12, 0);

    private Agent supervisor;
    private Agent agent;

    @BeforeEach
    void setUp() throws Exception {
        Clock fixedClock = Clock.fixed(FIXED_NOW.atZone(ZONE).toInstant(), ZONE);
        ticketService = new TicketService(ticketRepository, ticketHistoryRepository, agentService, fixedClock);

        supervisor = new Agent("sophie", "Sophie Martin", AgentRole.SUPERVISOR);
        setId(supervisor, 1L);
        agent = new Agent("karim", "Karim Bensalem", AgentRole.AGENT);
        setId(agent, 2L);
    }

    private static void setId(Object entity, Long id) throws Exception {
        Field field = entity.getClass().getDeclaredField("id");
        field.setAccessible(true);
        field.set(entity, id);
    }

    private Ticket newTicket(TicketStatus status, LocalDateTime createdAt) throws Exception {
        Ticket ticket = new Ticket("Panne imprimante", "desc", TicketPriority.MEDIUM, agent);
        setId(ticket, 100L);
        Field statusField = Ticket.class.getDeclaredField("status");
        statusField.setAccessible(true);
        statusField.set(ticket, status);
        Field createdAtField = Ticket.class.getDeclaredField("createdAt");
        createdAtField.setAccessible(true);
        createdAtField.set(ticket, createdAt);
        return ticket;
    }

    // --- Workflow de statuts ---

    @Test
    void transitionOpenVersInProgressEstAutorisee() {
        assertDoesNotThrow(() -> ticketService.checkTransition(TicketStatus.OPEN, TicketStatus.IN_PROGRESS));
    }

    @Test
    void transitionInProgressVersResolvedEstAutorisee() {
        assertDoesNotThrow(() -> ticketService.checkTransition(TicketStatus.IN_PROGRESS, TicketStatus.RESOLVED));
    }

    @Test
    void transitionResolvedVersClosedEstAutorisee() {
        assertDoesNotThrow(() -> ticketService.checkTransition(TicketStatus.RESOLVED, TicketStatus.CLOSED));
    }

    @Test
    void transitionDirecteOpenVersClosedEstInterdite() {
        assertThrows(InvalidStatusTransitionException.class,
                () -> ticketService.checkTransition(TicketStatus.OPEN, TicketStatus.CLOSED));
    }

    @Test
    void transitionDirecteOpenVersResolvedEstInterdite() {
        assertThrows(InvalidStatusTransitionException.class,
                () -> ticketService.checkTransition(TicketStatus.OPEN, TicketStatus.RESOLVED));
    }

    @Test
    void transitionEnArriereEstInterdite() {
        assertThrows(InvalidStatusTransitionException.class,
                () -> ticketService.checkTransition(TicketStatus.IN_PROGRESS, TicketStatus.OPEN));
    }

    @Test
    void changeStatusEnregistreUneEntreeDHistorique() throws Exception {
        Ticket ticket = newTicket(TicketStatus.OPEN, FIXED_NOW.minusHours(2));
        when(ticketRepository.findById(ticket.getId())).thenReturn(Optional.of(ticket));

        Ticket updated = ticketService.changeStatus(ticket.getId(), TicketStatus.IN_PROGRESS);

        assertEquals(TicketStatus.IN_PROGRESS, updated.getStatus());
    }

    @Test
    void changeStatusAvecSautLeveUneException() throws Exception {
        Ticket ticket = newTicket(TicketStatus.OPEN, FIXED_NOW.minusHours(2));
        when(ticketRepository.findById(ticket.getId())).thenReturn(Optional.of(ticket));

        assertThrows(InvalidStatusTransitionException.class,
                () -> ticketService.changeStatus(ticket.getId(), TicketStatus.CLOSED));
    }

    // --- Reassignation ---

    @Test
    void supervisorPeutReassignerUnTicket() throws Exception {
        Ticket ticket = newTicket(TicketStatus.OPEN, FIXED_NOW.minusHours(1));
        Agent otherAgent = new Agent("lea", "Lea Dubois", AgentRole.AGENT);
        setId(otherAgent, 3L);

        when(agentService.requireById(supervisor.getId())).thenReturn(supervisor);
        when(ticketRepository.findById(ticket.getId())).thenReturn(Optional.of(ticket));
        when(agentService.requireById(otherAgent.getId())).thenReturn(otherAgent);

        Ticket result = ticketService.reassign(supervisor.getId(), ticket.getId(), otherAgent.getId());

        assertEquals(otherAgent.getId(), result.getAssignedAgent().getId());
    }

    @Test
    void agentSimpleNePeutPasReassignerUnTicket() {
        when(agentService.requireById(agent.getId())).thenReturn(agent);

        assertThrows(ForbiddenOperationException.class,
                () -> ticketService.reassign(agent.getId(), 100L, 3L));
    }

    // --- Reporting : temps moyen de resolution ---

    @Test
    void moyenneDeResolutionCalculeeSurLesTicketsResolus() throws Exception {
        when(agentService.listAll()).thenReturn(List.of(agent));

        Ticket t1 = newTicket(TicketStatus.RESOLVED, FIXED_NOW.minusHours(3));
        setId(t1, 1L);
        Ticket t2 = newTicket(TicketStatus.RESOLVED, FIXED_NOW.minusHours(5));
        setId(t2, 2L);
        when(ticketRepository.findByAssignedAgentId(agent.getId())).thenReturn(List.of(t1, t2));

        TicketHistory h1 = new TicketHistory(t1, TicketStatus.IN_PROGRESS, TicketStatus.RESOLVED,
                t1.getCreatedAt().plusMinutes(60));
        TicketHistory h2 = new TicketHistory(t2, TicketStatus.IN_PROGRESS, TicketStatus.RESOLVED,
                t2.getCreatedAt().plusMinutes(120));
        when(ticketHistoryRepository.findByTicketIdOrderByChangedAtAsc(t1.getId())).thenReturn(List.of(h1));
        when(ticketHistoryRepository.findByTicketIdOrderByChangedAtAsc(t2.getId())).thenReturn(List.of(h2));

        List<ResolutionReport> reports = ticketService.averageResolutionTimePerAgent();

        assertEquals(1, reports.size());
        ResolutionReport report = reports.get(0);
        assertEquals(agent.getId(), report.agentId());
        assertEquals(2, report.resolvedTicketCount());
        assertEquals(90.0, report.averageResolutionMinutes());
    }

    @Test
    void ticketJamaisResoluEstExcluDeLaMoyenne() throws Exception {
        when(agentService.listAll()).thenReturn(List.of(agent));

        Ticket openTicket = newTicket(TicketStatus.OPEN, FIXED_NOW.minusHours(1));
        setId(openTicket, 5L);
        when(ticketRepository.findByAssignedAgentId(agent.getId())).thenReturn(List.of(openTicket));
        when(ticketHistoryRepository.findByTicketIdOrderByChangedAtAsc(openTicket.getId()))
                .thenReturn(new ArrayList<>());

        List<ResolutionReport> reports = ticketService.averageResolutionTimePerAgent();

        assertTrue(reports.isEmpty());
    }
}
