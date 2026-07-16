package com.jihedapps.notesapp.controller;

import com.jihedapps.notesapp.dto.AttachmentResponse;
import com.jihedapps.notesapp.service.AttachmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AttachmentController {

    private final AttachmentService attachmentService;

    @PostMapping("/notes/{noteId}/attachments")
    @ResponseStatus(HttpStatus.CREATED)
    public AttachmentResponse upload(@PathVariable Long noteId,
                                     @RequestParam("file") MultipartFile file,
                                     Authentication auth) {
        return attachmentService.upload(noteId, file, auth.getName());
    }

    @GetMapping("/attachments/{id}")
    public ResponseEntity<InputStreamResource> download(@PathVariable Long id, Authentication auth) {
        AttachmentService.DownloadedFile file = attachmentService.download(id, auth.getName());
        MediaType mediaType = file.contentType() != null
                ? MediaType.parseMediaType(file.contentType())
                : MediaType.APPLICATION_OCTET_STREAM;
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.filename() + "\"")
                .contentType(mediaType)
                .body(new InputStreamResource(file.stream()));
    }

    @DeleteMapping("/attachments/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, Authentication auth) {
        attachmentService.delete(id, auth.getName());
    }
}
