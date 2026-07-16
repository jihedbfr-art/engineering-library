package com.jihedapps.notesapp.dto;

import com.jihedapps.notesapp.model.Tag;

public record TagResponse(Long id, String name) {

    public static TagResponse from(Tag tag) {
        return new TagResponse(tag.getId(), tag.getName());
    }
}
