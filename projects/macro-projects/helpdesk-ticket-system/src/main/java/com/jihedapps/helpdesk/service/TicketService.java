package com.jihedapps.helpdesk.service;

import com.jihedapps.helpdesk.entity.*;
import com.jihedapps.helpdesk.exception.ForbiddenOperationException;
import com.jihedapps.helpdesk.exception.InvalidStatusTransitionException;
import com.jihedapps.helpdesk.exception.ResourceNotFoundException;
import com.jihedapps.helpdesk.repository.TicketHistoryRepository;
import com.jihedapps.helpdesk.repository.TicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final TicketHistoryRepository ticketHistoryRepository;
    private final AgentService agentService;
    private final Clock clock;

    public TicketService(TicketRepository ticketRepository, TicketHistoryRepository ticketHistoryRepository,
                          AgentService agentService, Clock clock) {
        this.ticketRepository = ticketRepository;
        this.ticketHistoryRepository = ticketHistoryRepository;
        this.agentService = agentService;
        this.clock = clock;
    }

    @Transactional
    public Ticket createTicket(String title, String description, TicketPriority priority, Long assignedAgentId) {
        Agent assignedAgent = assignedAgentId != null ? agentService.requireById(assignedAgentId) : null;
        Ticket ticket = new Ticket(title, description, priority, assignedAgent);
        return ticketRepository.save(ticket);
    }

    @Transactional(readOnly = true)
    public Ticket requireById(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket introuvable : id=" + id));
    }

    @Transactional(readOnly = true)
    public List<Ticket> listAll() {
        return ticketRepository.findAll();
    }

    /**
     * Regle metier : workflow de statuts strict et sequentiel. Un ticket ne
     * peut avancer que vers le statut immediatement suivant dans l'ordre
     * OPEN -> IN_PROGRESS -> RESOLVED -> CLOSED. Aucun saut, aucun retour en
     * arriere. Un ticket CLOSED est terminal.
     */
    public void checkTransition(TicketStatus from, TicketStatus to) {
        TicketStatus[] ordered = TicketStatus.values();
        int fromIndex = from.ordinal();
        int toIndex = to.ordinal();
        if (toIndex != fromIndex + 1) {
            throw new InvalidStatusTransitionException(
                    "Transition invalide : " + from + " -> " + to
                            + ". Le workflow impose l'ordre " + describe(ordered) + " sans saut ni retour en arriere.");
        }
    }

    private String describe(TicketStatus[] statuses) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < statuses.length; i++) {
            if (i > 0) {
                sb.append(" -> ");
            }
            sb.append(statuses[i]);
        }
        return sb.toString();
    }

    @Transactional
    public Ticket changeStatus(Long ticketId, TicketStatus newStatus) {
        Ticket ticket = requireById(ticketId);
        TicketStatus previousStatus = ticket.getStatus();
        checkTransition(previousStatus, newStatus);

        ticket.setStatus(newStatus);
        LocalDateTime now = LocalDateTime.now(clock);
        ticketHistoryRepository.save(new TicketHistory(ticket, previousStatus, newStatus, now));
        return ticket;
    }

    /**
     * Regle metier : seul un SUPERVISOR peut reassigner un ticket a un autre
     * agent. Un AGENT ne peut pas modifier l'assignation.
     */
    @Transactional
    public Ticket reassign(Long requesterId, Long ticketId, Long newAgentId) {
        Agent requester = agentService.requireById(requesterId);
        if (!requester.isSupervisor()) {
            throw new ForbiddenOperationException("Seul un SUPERVISOR peut reassigner un ticket.");
        }
        Ticket ticket = requireById(ticketId);
        Agent newAgent = agentService.requireById(newAgentId);
        ticket.setAssignedAgent(newAgent);
        return ticket;
    }

    @Transactional(readOnly = true)
    public List<Ticket> listByAgent(Long agentId) {
        return ticketRepository.findByAssignedAgentId(agentId);
    }

    @Transactional(readOnly = true)
    public List<TicketHistory> history(Long ticketId) {
        return ticketHistoryRepository.findByTicketIdOrderByChangedAtAsc(ticketId);
    }

    /**
     * Reporting : temps moyen de resolution par agent, en minutes. Pour
     * chaque ticket assigne a l'agent, le temps de resolution est la duree
     * entre sa creation et la premiere transition vers RESOLVED. Les tickets
     * jamais passes par RESOLVED ne comptent pas dans la moyenne.
     */
    @Transactional(readOnly = true)
    public List<com.jihedapps.helpdesk.dto.ResolutionReport> averageResolutionTimePerAgent() {
        List<Agent> agents = agentService.listAll();
        return agents.stream()
                .map(this::buildReportForAgent)
                .filter(r -> r.resolvedTicketCount() > 0)
                .sorted(Comparator.comparing(com.jihedapps.helpdesk.dto.ResolutionReport::agentUsername))
                .collect(Collectors.toList());
    }

    private com.jihedapps.helpdesk.dto.ResolutionReport buildReportForAgent(Agent agent) {
        List<Ticket> tickets = ticketRepository.findByAssignedAgentId(agent.getId());
        List<Long> resolutionMinutes = tickets.stream()
                .map(this::resolutionMinutesFor)
                .filter(m -> m != null)
                .collect(Collectors.toList());

        double average = resolutionMinutes.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0.0);

        return new com.jihedapps.helpdesk.dto.ResolutionReport(
                agent.getId(), agent.getUsername(), resolutionMinutes.size(), average);
    }

    private Long resolutionMinutesFor(Ticket ticket) {
        return ticketHistoryRepository.findByTicketIdOrderByChangedAtAsc(ticket.getId()).stream()
                .filter(h -> h.getToStatus() == TicketStatus.RESOLVED)
                .findFirst()
                .map(h -> ChronoUnit.MINUTES.between(ticket.getCreatedAt(), h.getChangedAt()))
                .orElse(null);
    }
}
