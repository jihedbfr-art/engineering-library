package com.jihedapps.helpdesk.repository;

import com.jihedapps.helpdesk.entity.TicketHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketHistoryRepository extends JpaRepository<TicketHistory, Long> {

    List<TicketHistory> findByTicketIdOrderByChangedAtAsc(Long ticketId);

    List<TicketHistory> findByTicketAssignedAgentId(Long agentId);
}
