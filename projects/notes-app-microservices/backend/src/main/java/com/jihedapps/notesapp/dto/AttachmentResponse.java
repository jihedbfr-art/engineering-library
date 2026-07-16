package com.jihedapps.notesapp.dto;

import com.jihedapps.notesapp.model.Attachment;

import java.time.Instant;

public record AttachmentResponse(
        Long id,
        String filename,
        String contentType,
        long size,
        Instant createdAt
) {
    public static AttachmentResponse from(Attachment attachment) {
        return new AttachmentResponse(
                attachment.getId(),
                attachment.getFilename(),
                attachment.getContentType(),
                attachment.getSize(),
                attachment.getCreatedAt()
        );
    }
}
