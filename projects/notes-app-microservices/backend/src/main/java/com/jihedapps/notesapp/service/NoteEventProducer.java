package com.jihedapps.notesapp.service;

import com.jihedapps.notesapp.dto.NoteEvent;
import com.jihedapps.notesapp.model.Note;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class NoteEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${notesapp.kafka.topic-note-events}")
    private String topic;

    public void publish(String eventType, Note note) {
        NoteEvent event = new NoteEvent(eventType, note.getId(), note.getTitle(), note.getOwner(), Instant.now());
        kafkaTemplate.send(topic, note.getOwner(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.warn("Echec de publication Kafka {} pour la note {}: {}", eventType, note.getId(), ex.getMessage());
                    }
                });
    }
}
