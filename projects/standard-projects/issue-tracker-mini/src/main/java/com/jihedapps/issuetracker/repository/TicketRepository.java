package com.jihedapps.issuetracker.repository;

import com.jihedapps.issuetracker.entity.Ticket;
import com.jihedapps.issuetracker.entity.TicketPriority;
import com.jihedapps.issuetracker.entity.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findByProjectId(Long projectId);

    List<Ticket> findByProjectIdAndStatus(Long projectId, TicketStatus status);

    List<Ticket> findByProjectIdAndPriority(Long projectId, TicketPriority priority);

    List<Ticket> findByProjectIdAndStatusAndPriority(Long projectId, TicketStatus status, TicketPriority priority);

    List<Ticket> findByStatus(TicketStatus status);

    List<Ticket> findByPriority(TicketPriority priority);
}
