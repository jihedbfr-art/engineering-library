package com.jihedapps.helpdesk.dto;

import com.jihedapps.helpdesk.entity.Ticket;
import com.jihedapps.helpdesk.entity.TicketPriority;
import com.jihedapps.helpdesk.entity.TicketStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class TicketDtos {

    public record CreateTicketRequest(
            @NotBlank String title,
            String description,
            @NotNull TicketPriority priority,
            Long assignedAgentId) {
    }

    public record ChangeStatusRequest(
            @NotNull TicketStatus status) {
    }

    public record ReassignRequest(
            @NotNull Long newAgentId) {
    }

    public record TicketResponse(
            Long id,
            String title,
            String description,
            TicketPriority priority,
            TicketStatus status,
            Long assignedAgentId,
            String assignedAgentUsername,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {

        public static TicketResponse from(Ticket ticket) {
            return new TicketResponse(
                    ticket.getId(),
                    ticket.getTitle(),
                    ticket.getDescription(),
                    ticket.getPriority(),
                    ticket.getStatus(),
                    ticket.getAssignedAgent() != null ? ticket.getAssignedAgent().getId() : null,
                    ticket.getAssignedAgent() != null ? ticket.getAssignedAgent().getUsername() : null,
                    ticket.getCreatedAt(),
                    ticket.getUpdatedAt());
        }
    }
}
