package com.jihedapps.helpdesk.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "agent")
public class Agent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AgentRole role;

    protected Agent() {
    }

    public Agent(String username, String fullName, AgentRole role) {
        this.username = username;
        this.fullName = fullName;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getFullName() {
        return fullName;
    }

    public AgentRole getRole() {
        return role;
    }

    public void setRole(AgentRole role) {
        this.role = role;
    }

    public boolean isSupervisor() {
        return role == AgentRole.SUPERVISOR;
    }
}
