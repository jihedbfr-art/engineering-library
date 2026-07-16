package com.jihedapps.notesapp.service;

import com.jihedapps.notesapp.dto.NotebookRequest;
import com.jihedapps.notesapp.dto.NotebookResponse;
import com.jihedapps.notesapp.model.Notebook;
import com.jihedapps.notesapp.repository.NotebookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotebookService {

    private final NotebookRepository notebookRepository;

    @Transactional(readOnly = true)
    public List<NotebookResponse> list(String owner) {
        return notebookRepository.findByOwnerOrderByNameAsc(owner)
                .stream().map(NotebookResponse::from).toList();
    }

    @Transactional
    public NotebookResponse create(NotebookRequest request, String owner) {
        if (notebookRepository.existsByNameAndOwner(request.name().trim(), owner)) {
            throw new IllegalArgumentException("Un carnet porte déjà ce nom");
        }
        Notebook notebook = notebookRepository.save(
                Notebook.builder().name(request.name().trim()).owner(owner).build());
        return NotebookResponse.from(notebook);
    }

    @Transactional
    public NotebookResponse rename(Long id, NotebookRequest request, String owner) {
        Notebook notebook = findOwned(id, owner);
        notebook.setName(request.name().trim());
        return NotebookResponse.from(notebookRepository.save(notebook));
    }

    /** Supprime le carnet ; ses notes sont détachées (elles restent dans "Notes"). */
    @Transactional
    public void delete(Long id, String owner) {
        Notebook notebook = findOwned(id, owner);
        notebook.getNotes().forEach(note -> note.setNotebook(null));
        notebookRepository.delete(notebook);
    }

    private Notebook findOwned(Long id, String owner) {
        return notebookRepository.findByIdAndOwner(id, owner)
                .orElseThrow(() -> new NotFoundException("Carnet introuvable: " + id));
    }
}
