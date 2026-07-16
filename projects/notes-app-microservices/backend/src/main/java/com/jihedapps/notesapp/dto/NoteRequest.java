package com.jihedapps.notesapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record NoteRequest(
        @NotBlank @Size(max = 255) String title,
        String content,
        Long notebookId,
        Boolean pinned,
        List<String> tags
) {
}
