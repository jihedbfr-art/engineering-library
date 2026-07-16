package com.jihedapps.helpdesk.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Trace immuable de chaque changement de statut d'un ticket. Une ligne par
 * transition, jamais modifiee ni supprimee apres creation.
 */
@Entity
@Table(name = "ticket_history")
public class TicketHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketStatus toStatus;

    @Column(nullable = false)
    private LocalDateTime changedAt = LocalDateTime.now();

    protected TicketHistory() {
    }

    public TicketHistory(Ticket ticket, TicketStatus fromStatus, TicketStatus toStatus, LocalDateTime changedAt) {
        this.ticket = ticket;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.changedAt = changedAt;
    }

    public Long getId() {
        return id;
    }

    public Ticket getTicket() {
        return ticket;
    }

    public TicketStatus getFromStatus() {
        return fromStatus;
    }

    public TicketStatus getToStatus() {
        return toStatus;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }
}
