package com.jihedapps.issuetracker.controller;

import com.jihedapps.issuetracker.entity.Project;
import com.jihedapps.issuetracker.entity.TicketPriority;
import com.jihedapps.issuetracker.entity.TicketStatus;
import com.jihedapps.issuetracker.service.ProjectService;
import com.jihedapps.issuetracker.service.TicketService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class TicketBoardController {

    private final ProjectService projectService;
    private final TicketService ticketService;

    public TicketBoardController(ProjectService projectService, TicketService ticketService) {
        this.projectService = projectService;
        this.ticketService = ticketService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("projects", projectService.findAll());
        return "projects";
    }

    @GetMapping("/projects/{id}/tickets")
    public String tickets(@PathVariable Long id,
                           @RequestParam(required = false) TicketStatus status,
                           @RequestParam(required = false) TicketPriority priority,
                           Model model) {
        Project project = projectService.findById(id);
        model.addAttribute("project", project);
        model.addAttribute("tickets", ticketService.search(id, status, priority));
        model.addAttribute("statuses", TicketStatus.values());
        model.addAttribute("priorities", TicketPriority.values());
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedPriority", priority);
        return "tickets";
    }

    @PostMapping("/projects/{projectId}/tickets/{ticketId}/status")
    public String changeStatus(@PathVariable Long projectId,
                                @PathVariable Long ticketId,
                                @RequestParam TicketStatus status) {
        ticketService.updateStatus(ticketId, status);
        return "redirect:/projects/" + projectId + "/tickets";
    }
}
