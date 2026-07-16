package com.jihedapps.notesapp.repository;

import com.jihedapps.notesapp.model.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {

    Optional<Attachment> findByIdAndNoteOwner(Long id, String owner);
}
