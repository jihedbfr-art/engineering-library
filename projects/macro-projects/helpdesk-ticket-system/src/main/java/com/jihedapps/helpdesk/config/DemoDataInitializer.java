package com.jihedapps.helpdesk.config;

import com.jihedapps.helpdesk.entity.Agent;
import com.jihedapps.helpdesk.entity.AgentRole;
import com.jihedapps.helpdesk.repository.AgentRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DemoDataInitializer implements CommandLineRunner {

    private final AgentRepository agentRepository;

    public DemoDataInitializer(AgentRepository agentRepository) {
        this.agentRepository = agentRepository;
    }

    @Override
    public void run(String... args) {
        if (agentRepository.count() > 0) {
            return;
        }
        agentRepository.save(new Agent("sophie", "Sophie Martin", AgentRole.SUPERVISOR));
        agentRepository.save(new Agent("karim", "Karim Bensalem", AgentRole.AGENT));
        agentRepository.save(new Agent("lea", "Lea Dubois", AgentRole.AGENT));
    }
}
