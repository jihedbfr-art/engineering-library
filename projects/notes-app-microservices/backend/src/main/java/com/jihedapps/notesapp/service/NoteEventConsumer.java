package com.jihedapps.notesapp.service;

import com.jihedapps.notesapp.dto.NoteEvent;
import com.jihedapps.notesapp.model.ActivityLog;
import com.jihedapps.notesapp.repository.ActivityLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NoteEventConsumer {

    private final ActivityLogRepository activityLogRepository;

    @KafkaListener(topics = "${notesapp.kafka.topic-note-events}")
    public void onNoteEvent(NoteEvent event) {
        log.info("Evenement note recu: {} note={} user={}", event.eventType(), event.noteId(), event.username());
        activityLogRepository.save(ActivityLog.builder()
                .eventType(event.eventType())
                .noteId(event.noteId())
                .noteTitle(event.noteTitle())
                .username(event.username())
                .occurredAt(event.occurredAt())
                .build());
    }
}
