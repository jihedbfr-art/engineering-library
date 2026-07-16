package com.jihedapps.helpdesk.service;

import com.jihedapps.helpdesk.entity.Agent;
import com.jihedapps.helpdesk.exception.ResourceNotFoundException;
import com.jihedapps.helpdesk.repository.AgentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AgentService {

    private final AgentRepository agentRepository;

    public AgentService(AgentRepository agentRepository) {
        this.agentRepository = agentRepository;
    }

    @Transactional
    public Agent createAgent(String username, String fullName, com.jihedapps.helpdesk.entity.AgentRole role) {
        Agent agent = new Agent(username, fullName, role);
        return agentRepository.save(agent);
    }

    @Transactional(readOnly = true)
    public Agent requireById(Long id) {
        return agentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agent introuvable : id=" + id));
    }

    @Transactional(readOnly = true)
    public List<Agent> listAll() {
        return agentRepository.findAll();
    }
}
