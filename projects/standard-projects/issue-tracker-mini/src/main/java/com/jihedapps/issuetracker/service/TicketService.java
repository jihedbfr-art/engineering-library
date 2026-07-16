package com.jihedapps.issuetracker.service;

import com.jihedapps.issuetracker.entity.Project;
import com.jihedapps.issuetracker.entity.Ticket;
import com.jihedapps.issuetracker.entity.TicketPriority;
import com.jihedapps.issuetracker.entity.TicketStatus;
import com.jihedapps.issuetracker.exception.ResourceNotFoundException;
import com.jihedapps.issuetracker.repository.TicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class TicketService {

    private final TicketRepository ticketRepository;
    private final ProjectService projectService;

    public TicketService(TicketRepository ticketRepository, ProjectService projectService) {
        this.ticketRepository = ticketRepository;
        this.projectService = projectService;
    }

    @Transactional(readOnly = true)
    public List<Ticket> findAll() {
        return ticketRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Ticket findById(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket introuvable : " + id));
    }

    @Transactional(readOnly = true)
    public List<Ticket> search(Long projectId, TicketStatus status, TicketPriority priority) {
        if (projectId != null && status != null && priority != null) {
            return ticketRepository.findByProjectIdAndStatusAndPriority(projectId, status, priority);
        }
        if (projectId != null && status != null) {
            return ticketRepository.findByProjectIdAndStatus(projectId, status);
        }
        if (projectId != null && priority != null) {
            return ticketRepository.findByProjectIdAndPriority(projectId, priority);
        }
        if (projectId != null) {
            return ticketRepository.findByProjectId(projectId);
        }
        if (status != null) {
            return ticketRepository.findByStatus(status);
        }
        if (priority != null) {
            return ticketRepository.findByPriority(priority);
        }
        return ticketRepository.findAll();
    }

    public Ticket create(Long projectId, Ticket payload) {
        Project project = projectService.findById(projectId);
        payload.setId(null);
        payload.setProject(project);
        return ticketRepository.save(payload);
    }

    public Ticket update(Long id, Ticket payload) {
        Ticket existing = findById(id);
        existing.setTitle(payload.getTitle());
        existing.setDescription(payload.getDescription());
        existing.setStatus(payload.getStatus());
        existing.setPriority(payload.getPriority());
        return ticketRepository.save(existing);
    }

    public Ticket updateStatus(Long id, TicketStatus status) {
        Ticket existing = findById(id);
        existing.setStatus(status);
        return ticketRepository.save(existing);
    }

    public void delete(Long id) {
        Ticket existing = findById(id);
        ticketRepository.delete(existing);
    }
}
