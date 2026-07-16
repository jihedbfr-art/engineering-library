package com.jihedapps.helpdesk.repository;

import com.jihedapps.helpdesk.entity.Agent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgentRepository extends JpaRepository<Agent, Long> {
}
