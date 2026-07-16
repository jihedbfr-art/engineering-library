package com.jihedapps.notesapp.controller;

import com.jihedapps.notesapp.dto.NotebookRequest;
import com.jihedapps.notesapp.dto.NotebookResponse;
import com.jihedapps.notesapp.service.NotebookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notebooks")
@RequiredArgsConstructor
public class NotebookController {

    private final NotebookService notebookService;

    @GetMapping
    public List<NotebookResponse> list(Authentication auth) {
        return notebookService.list(auth.getName());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public NotebookResponse create(@Valid @RequestBody NotebookRequest request, Authentication auth) {
        return notebookService.create(request, auth.getName());
    }

    @PutMapping("/{id}")
    public NotebookResponse rename(@PathVariable Long id, @Valid @RequestBody NotebookRequest request, Authentication auth) {
        return notebookService.rename(id, request, auth.getName());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, Authentication auth) {
        notebookService.delete(id, auth.getName());
    }
}
