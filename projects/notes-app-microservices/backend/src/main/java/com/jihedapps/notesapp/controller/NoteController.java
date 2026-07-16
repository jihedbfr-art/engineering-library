package com.jihedapps.notesapp.controller;

import com.jihedapps.notesapp.dto.NoteRequest;
import com.jihedapps.notesapp.dto.NoteResponse;
import com.jihedapps.notesapp.service.NoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;

    @GetMapping
    public List<NoteResponse> list(Authentication auth,
                                   @RequestParam(required = false) Long notebookId,
                                   @RequestParam(required = false) Long tagId,
                                   @RequestParam(required = false, name = "q") String query) {
        return noteService.list(auth.getName(), notebookId, tagId, query);
    }

    @GetMapping("/trash")
    public List<NoteResponse> trash(Authentication auth) {
        return noteService.trash(auth.getName());
    }

    @GetMapping("/{id}")
    public NoteResponse get(@PathVariable Long id, Authentication auth) {
        return noteService.get(id, auth.getName());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public NoteResponse create(@Valid @RequestBody NoteRequest request, Authentication auth) {
        return noteService.create(request, auth.getName());
    }

    @PutMapping("/{id}")
    public NoteResponse update(@PathVariable Long id, @Valid @RequestBody NoteRequest request, Authentication auth) {
        return noteService.update(id, request, auth.getName());
    }

    /** Soft delete : envoie à la corbeille. */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void moveToTrash(@PathVariable Long id, Authentication auth) {
        noteService.moveToTrash(id, auth.getName());
    }

    @PostMapping("/{id}/restore")
    public NoteResponse restore(@PathVariable Long id, Authentication auth) {
        return noteService.restore(id, auth.getName());
    }

    /** Suppression définitive depuis la corbeille. */
    @DeleteMapping("/{id}/purge")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void purge(@PathVariable Long id, Authentication auth) {
        noteService.purge(id, auth.getName());
    }
}
