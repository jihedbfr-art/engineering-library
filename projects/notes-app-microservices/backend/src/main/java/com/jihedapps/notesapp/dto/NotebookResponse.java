package com.jihedapps.notesapp.dto;

import com.jihedapps.notesapp.model.Notebook;

import java.time.Instant;

public record NotebookResponse(Long id, String name, Instant createdAt, long noteCount) {

    public static NotebookResponse from(Notebook notebook) {
        long count = notebook.getNotes().stream().filter(n -> !n.isDeleted()).count();
        return new NotebookResponse(notebook.getId(), notebook.getName(), notebook.getCreatedAt(), count);
    }
}
