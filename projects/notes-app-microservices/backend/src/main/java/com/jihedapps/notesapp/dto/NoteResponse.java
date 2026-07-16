package com.jihedapps.notesapp.dto;

import com.jihedapps.notesapp.model.Note;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

public record NoteResponse(
        Long id,
        String title,
        String content,
        boolean pinned,
        boolean deleted,
        Instant createdAt,
        Instant updatedAt,
        Instant deletedAt,
        Long notebookId,
        String notebookName,
        List<String> tags,
        List<AttachmentResponse> attachments
) {
    public static NoteResponse from(Note note) {
        return new NoteResponse(
                note.getId(),
                note.getTitle(),
                note.getContent(),
                note.isPinned(),
                note.isDeleted(),
                note.getCreatedAt(),
                note.getUpdatedAt(),
                note.getDeletedAt(),
                note.getNotebook() != null ? note.getNotebook().getId() : null,
                note.getNotebook() != null ? note.getNotebook().getName() : null,
                note.getTags().stream().map(t -> t.getName()).sorted().toList(),
                note.getAttachments().stream()
                        .sorted(Comparator.comparing(a -> a.getCreatedAt()))
                        .map(AttachmentResponse::from)
                        .toList()
        );
    }
}
