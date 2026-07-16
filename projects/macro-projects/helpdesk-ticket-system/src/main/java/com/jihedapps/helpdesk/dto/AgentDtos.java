package com.jihedapps.helpdesk.dto;

import com.jihedapps.helpdesk.entity.Agent;
import com.jihedapps.helpdesk.entity.AgentRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class AgentDtos {

    public record CreateAgentRequest(
            @NotBlank String username,
            @NotBlank String fullName,
            @NotNull AgentRole role) {
    }

    public record AgentResponse(
            Long id,
            String username,
            String fullName,
            AgentRole role) {

        public static AgentResponse from(Agent agent) {
            return new AgentResponse(agent.getId(), agent.getUsername(), agent.getFullName(), agent.getRole());
        }
    }
}
