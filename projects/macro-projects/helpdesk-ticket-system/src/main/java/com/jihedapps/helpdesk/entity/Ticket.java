package com.jihedapps.helpdesk.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ticket")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketPriority priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketStatus status = TicketStatus.OPEN;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_agent_id")
    private Agent assignedAgent;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    protected Ticket() {
    }

    public Ticket(String title, String description, TicketPriority priority, Agent assignedAgent) {
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.assignedAgent = assignedAgent;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TicketPriority getPriority() {
        return priority;
    }

    public void setPriority(TicketPriority priority) {
        this.priority = priority;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public void setStatus(TicketStatus status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }

    public Agent getAssignedAgent() {
        return assignedAgent;
    }

    public void setAssignedAgent(Agent assignedAgent) {
        this.assignedAgent = assignedAgent;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
