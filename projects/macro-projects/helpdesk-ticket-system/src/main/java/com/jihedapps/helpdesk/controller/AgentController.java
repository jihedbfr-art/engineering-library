package com.jihedapps.helpdesk.controller;

import com.jihedapps.helpdesk.dto.AgentDtos.AgentResponse;
import com.jihedapps.helpdesk.dto.AgentDtos.CreateAgentRequest;
import com.jihedapps.helpdesk.service.AgentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/agents")
public class AgentController {

    private final AgentService agentService;

    public AgentController(AgentService agentService) {
        this.agentService = agentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AgentResponse createAgent(@Valid @RequestBody CreateAgentRequest request) {
        return AgentResponse.from(agentService.createAgent(request.username(), request.fullName(), request.role()));
    }

    @GetMapping
    public List<AgentResponse> listAgents() {
        return agentService.listAll().stream().map(AgentResponse::from).toList();
    }

    @GetMapping("/{id}")
    public AgentResponse getAgent(@PathVariable Long id) {
        return AgentResponse.from(agentService.requireById(id));
    }
}
