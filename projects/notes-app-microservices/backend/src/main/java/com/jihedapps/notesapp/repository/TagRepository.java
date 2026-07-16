package com.jihedapps.notesapp.repository;

import com.jihedapps.notesapp.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {

    List<Tag> findByOwnerOrderByNameAsc(String owner);

    Optional<Tag> findByNameIgnoreCaseAndOwner(String name, String owner);

    Optional<Tag> findByIdAndOwner(Long id, String owner);
}
