package com.jihedapps.notesapp.dto;

import com.jihedapps.notesapp.model.ActivityLog;

import java.time.Instant;

public record ActivityResponse(Long id, String eventType, Long noteId, String noteTitle, Instant occurredAt) {

    public static ActivityResponse from(ActivityLog log) {
        return new ActivityResponse(log.getId(), log.getEventType(), log.getNoteId(), log.getNoteTitle(), log.getOccurredAt());
    }
}
