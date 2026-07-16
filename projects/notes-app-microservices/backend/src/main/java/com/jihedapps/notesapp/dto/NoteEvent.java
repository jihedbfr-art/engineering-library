package com.jihedapps.notesapp.dto;

import java.time.Instant;

/** Événement métier publié sur le topic Kafka note-events. */
public record NoteEvent(
        String eventType,
        Long noteId,
        String noteTitle,
        String username,
        Instant occurredAt
) {
    public static final String CREATED = "NOTE_CREATED";
    public static final String UPDATED = "NOTE_UPDATED";
    public static final String TRASHED = "NOTE_TRASHED";
    public static final String RESTORED = "NOTE_RESTORED";
    public static final String PURGED = "NOTE_PURGED";
}
