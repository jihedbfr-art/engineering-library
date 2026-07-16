package com.jihedapps.helpdesk.controller;

import com.jihedapps.helpdesk.dto.ResolutionReport;
import com.jihedapps.helpdesk.dto.TicketDtos.ChangeStatusRequest;
import com.jihedapps.helpdesk.dto.TicketDtos.CreateTicketRequest;
import com.jihedapps.helpdesk.dto.TicketDtos.ReassignRequest;
import com.jihedapps.helpdesk.dto.TicketDtos.TicketResponse;
import com.jihedapps.helpdesk.entity.Ticket;
import com.jihedapps.helpdesk.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TicketResponse createTicket(@Valid @RequestBody CreateTicketRequest request) {
        Ticket ticket = ticketService.createTicket(request.title(), request.description(),
                request.priority(), request.assignedAgentId());
        return TicketResponse.from(ticket);
    }

    @GetMapping
    public List<TicketResponse> listTickets(@RequestParam(required = false) Long agentId) {
        List<Ticket> tickets = agentId != null ? ticketService.listByAgent(agentId) : ticketService.listAll();
        return tickets.stream().map(TicketResponse::from).toList();
    }

    @GetMapping("/{id}")
    public TicketResponse getTicket(@PathVariable Long id) {
        return TicketResponse.from(ticketService.requireById(id));
    }

    @PutMapping("/{id}/status")
    public TicketResponse changeStatus(@PathVariable Long id, @Valid @RequestBody ChangeStatusRequest request) {
        return TicketResponse.from(ticketService.changeStatus(id, request.status()));
    }

    @PutMapping("/{id}/reassign")
    public TicketResponse reassign(@RequestHeader("X-Agent-Id") Long requesterId,
                                    @PathVariable Long id,
                                    @Valid @RequestBody ReassignRequest request) {
        return TicketResponse.from(ticketService.reassign(requesterId, id, request.newAgentId()));
    }

    @GetMapping("/reports/average-resolution-time")
    public List<ResolutionReport> averageResolutionTime() {
        return ticketService.averageResolutionTimePerAgent();
    }
}
