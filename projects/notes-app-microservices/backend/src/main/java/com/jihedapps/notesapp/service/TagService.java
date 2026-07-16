package com.jihedapps.notesapp.service;

import com.jihedapps.notesapp.dto.TagResponse;
import com.jihedapps.notesapp.model.Tag;
import com.jihedapps.notesapp.repository.NoteRepository;
import com.jihedapps.notesapp.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;
    private final NoteRepository noteRepository;

    @Transactional(readOnly = true)
    public List<TagResponse> list(String owner) {
        return tagRepository.findByOwnerOrderByNameAsc(owner)
                .stream().map(TagResponse::from).toList();
    }

    @Transactional
    public void delete(Long id, String owner) {
        Tag tag = tagRepository.findByIdAndOwner(id, owner)
                .orElseThrow(() -> new NotFoundException("Etiquette introuvable: " + id));
        noteRepository.findByTag(owner, id).forEach(note -> note.getTags().remove(tag));
        tagRepository.delete(tag);
    }
}
