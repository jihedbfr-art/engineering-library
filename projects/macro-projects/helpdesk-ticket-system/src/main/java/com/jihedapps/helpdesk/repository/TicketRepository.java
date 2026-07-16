package com.jihedapps.helpdesk.repository;

import com.jihedapps.helpdesk.entity.Ticket;
import com.jihedapps.helpdesk.entity.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findByAssignedAgentId(Long agentId);

    List<Ticket> findByStatus(TicketStatus status);
}
