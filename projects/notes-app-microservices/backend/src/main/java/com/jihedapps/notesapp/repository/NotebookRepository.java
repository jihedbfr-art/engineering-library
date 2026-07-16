package com.jihedapps.notesapp.repository;

import com.jihedapps.notesapp.model.Notebook;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotebookRepository extends JpaRepository<Notebook, Long> {

    List<Notebook> findByOwnerOrderByNameAsc(String owner);

    Optional<Notebook> findByIdAndOwner(Long id, String owner);

    boolean existsByNameAndOwner(String name, String owner);
}
