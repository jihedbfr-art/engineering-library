package com.jihedapps.notesapp.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "attachments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String filename;

    private String contentType;

    private long size;

    /** Clé de l'objet dans le bucket MinIO. */
    @Column(nullable = false, unique = true)
    private String objectKey;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "note_id")
    private Note note;

    @PrePersist
    void prePersist() {
        createdAt = Instant.now();
    }
}
