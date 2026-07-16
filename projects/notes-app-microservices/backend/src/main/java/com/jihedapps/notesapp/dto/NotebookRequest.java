package com.jihedapps.notesapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NotebookRequest(@NotBlank @Size(max = 100) String name) {
}
