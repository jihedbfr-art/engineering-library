package com.jihedapps.notesapp.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/** Historique d'activité alimenté par le consumer Kafka (topic note-events). */
@Entity
@Table(name = "activity_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String eventType;

    private Long noteId;

    private String noteTitle;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private Instant occurredAt;
}
