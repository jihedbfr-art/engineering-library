package com.jihedapps.notesapp.service;

import com.jihedapps.notesapp.dto.NoteEvent;
import com.jihedapps.notesapp.dto.NoteRequest;
import com.jihedapps.notesapp.dto.NoteResponse;
import com.jihedapps.notesapp.model.Note;
import com.jihedapps.notesapp.model.Notebook;
import com.jihedapps.notesapp.model.Tag;
import com.jihedapps.notesapp.repository.NoteRepository;
import com.jihedapps.notesapp.repository.NotebookRepository;
import com.jihedapps.notesapp.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class NoteService {

    private final NoteRepository noteRepository;
    private final NotebookRepository notebookRepository;
    private final TagRepository tagRepository;
    private final AttachmentService attachmentService;
    private final NoteEventProducer eventProducer;

    @Transactional(readOnly = true)
    public List<NoteResponse> list(String owner, Long notebookId, Long tagId, String query) {
        List<Note> notes;
        if (query != null && !query.isBlank()) {
            notes = noteRepository.search(owner, query.trim());
        } else if (notebookId != null) {
            notes = noteRepository.findByOwnerAndNotebookIdAndDeletedFalseOrderByPinnedDescUpdatedAtDesc(owner, notebookId);
        } else if (tagId != null) {
            notes = noteRepository.findByTag(owner, tagId);
        } else {
            notes = noteRepository.findByOwnerAndDeletedFalseOrderByPinnedDescUpdatedAtDesc(owner);
        }
        return notes.stream().map(NoteResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public NoteResponse get(Long id, String owner) {
        return NoteResponse.from(findOwned(id, owner));
    }

    @Transactional
    public NoteResponse create(NoteRequest request, String owner) {
        Note note = Note.builder()
                .title(request.title())
                .content(request.content())
                .owner(owner)
                .pinned(Boolean.TRUE.equals(request.pinned()))
                .build();
        applyNotebookAndTags(note, request, owner);
        note = noteRepository.save(note);
        eventProducer.publish(NoteEvent.CREATED, note);
        return NoteResponse.from(note);
    }

    @Transactional
    public NoteResponse update(Long id, NoteRequest request, String owner) {
        Note note = findOwned(id, owner);
        note.setTitle(request.title());
        note.setContent(request.content());
        if (request.pinned() != null) {
            note.setPinned(request.pinned());
        }
        applyNotebookAndTags(note, request, owner);
        note = noteRepository.save(note);
        eventProducer.publish(NoteEvent.UPDATED, note);
        return NoteResponse.from(note);
    }

    /** Soft delete : envoie la note à la corbeille. */
    @Transactional
    public void moveToTrash(Long id, String owner) {
        Note note = findOwned(id, owner);
        note.setDeleted(true);
        note.setDeletedAt(Instant.now());
        noteRepository.save(note);
        eventProducer.publish(NoteEvent.TRASHED, note);
    }

    @Transactional
    public NoteResponse restore(Long id, String owner) {
        Note note = findOwned(id, owner);
        note.setDeleted(false);
        note.setDeletedAt(null);
        note = noteRepository.save(note);
        eventProducer.publish(NoteEvent.RESTORED, note);
        return NoteResponse.from(note);
    }

    /** Suppression définitive (purge de la corbeille) + nettoyage MinIO. */
    @Transactional
    public void purge(Long id, String owner) {
        Note note = findOwned(id, owner);
        note.getAttachments().forEach(a -> attachmentService.deleteObjectQuietly(a.getObjectKey()));
        eventProducer.publish(NoteEvent.PURGED, note);
        noteRepository.delete(note);
    }

    @Transactional(readOnly = true)
    public List<NoteResponse> trash(String owner) {
        return noteRepository.findByOwnerAndDeletedTrueOrderByDeletedAtDesc(owner)
                .stream().map(NoteResponse::from).toList();
    }

    private void applyNotebookAndTags(Note note, NoteRequest request, String owner) {
        if (request.notebookId() != null) {
            Notebook notebook = notebookRepository.findByIdAndOwner(request.notebookId(), owner)
                    .orElseThrow(() -> new NotFoundException("Carnet introuvable: " + request.notebookId()));
            note.setNotebook(notebook);
        } else {
            note.setNotebook(null);
        }
        if (request.tags() != null) {
            Set<Tag> tags = new HashSet<>();
            for (String name : request.tags()) {
                if (name == null || name.isBlank()) {
                    continue;
                }
                String trimmed = name.trim();
                Tag tag = tagRepository.findByNameIgnoreCaseAndOwner(trimmed, owner)
                        .orElseGet(() -> tagRepository.save(Tag.builder().name(trimmed).owner(owner).build()));
                tags.add(tag);
            }
            note.setTags(tags);
        }
    }

    Note findOwned(Long id, String owner) {
        return noteRepository.findByIdAndOwner(id, owner)
                .orElseThrow(() -> new NotFoundException("Note introuvable: " + id));
    }
}
