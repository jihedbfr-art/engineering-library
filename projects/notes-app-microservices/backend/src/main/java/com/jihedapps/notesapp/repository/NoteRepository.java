package com.jihedapps.notesapp.repository;

import com.jihedapps.notesapp.model.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NoteRepository extends JpaRepository<Note, Long> {

    List<Note> findByOwnerAndDeletedFalseOrderByPinnedDescUpdatedAtDesc(String owner);

    List<Note> findByOwnerAndDeletedTrueOrderByDeletedAtDesc(String owner);

    List<Note> findByOwnerAndNotebookIdAndDeletedFalseOrderByPinnedDescUpdatedAtDesc(String owner, Long notebookId);

    Optional<Note> findByIdAndOwner(Long id, String owner);

    @Query("""
            SELECT DISTINCT n FROM Note n LEFT JOIN n.tags t
            WHERE n.owner = :owner AND n.deleted = false
              AND (LOWER(n.title) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(n.content) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(t.name) LIKE LOWER(CONCAT('%', :q, '%')))
            ORDER BY n.pinned DESC, n.updatedAt DESC
            """)
    List<Note> search(@Param("owner") String owner, @Param("q") String query);

    @Query("""
            SELECT n FROM Note n JOIN n.tags t
            WHERE n.owner = :owner AND n.deleted = false AND t.id = :tagId
            ORDER BY n.pinned DESC, n.updatedAt DESC
            """)
    List<Note> findByTag(@Param("owner") String owner, @Param("tagId") Long tagId);
}
