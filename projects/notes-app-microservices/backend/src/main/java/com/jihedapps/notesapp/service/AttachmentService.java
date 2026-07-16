package com.jihedapps.notesapp.service;

import com.jihedapps.notesapp.dto.AttachmentResponse;
import com.jihedapps.notesapp.model.Attachment;
import com.jihedapps.notesapp.model.Note;
import com.jihedapps.notesapp.repository.AttachmentRepository;
import com.jihedapps.notesapp.repository.NoteRepository;
import io.minio.GetObjectArgs;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

import io.minio.MinioClient;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttachmentService {

    private final MinioClient minioClient;
    private final AttachmentRepository attachmentRepository;
    private final NoteRepository noteRepository;

    @Value("${minio.bucket}")
    private String bucket;

    @Transactional
    public AttachmentResponse upload(Long noteId, MultipartFile file, String owner) {
        Note note = noteRepository.findByIdAndOwner(noteId, owner)
                .orElseThrow(() -> new NotFoundException("Note introuvable: " + noteId));
        String objectKey = owner + "/" + noteId + "/" + UUID.randomUUID() + "-" + file.getOriginalFilename();
        try (InputStream in = file.getInputStream()) {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectKey)
                    .stream(in, file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());
        } catch (Exception e) {
            throw new IllegalStateException("Echec de l'upload vers MinIO", e);
        }
        Attachment attachment = attachmentRepository.save(Attachment.builder()
                .filename(file.getOriginalFilename())
                .contentType(file.getContentType())
                .size(file.getSize())
                .objectKey(objectKey)
                .note(note)
                .build());
        return AttachmentResponse.from(attachment);
    }

    @Transactional(readOnly = true)
    public DownloadedFile download(Long attachmentId, String owner) {
        Attachment attachment = findOwned(attachmentId, owner);
        try {
            InputStream stream = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(attachment.getObjectKey())
                    .build());
            return new DownloadedFile(attachment.getFilename(), attachment.getContentType(), stream);
        } catch (Exception e) {
            throw new IllegalStateException("Echec du téléchargement depuis MinIO", e);
        }
    }

    @Transactional
    public void delete(Long attachmentId, String owner) {
        Attachment attachment = findOwned(attachmentId, owner);
        deleteObjectQuietly(attachment.getObjectKey());
        attachment.getNote().getAttachments().remove(attachment);
        attachmentRepository.delete(attachment);
    }

    void deleteObjectQuietly(String objectKey) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucket).object(objectKey).build());
        } catch (Exception e) {
            log.warn("Impossible de supprimer l'objet MinIO {}: {}", objectKey, e.getMessage());
        }
    }

    private Attachment findOwned(Long id, String owner) {
        return attachmentRepository.findByIdAndNoteOwner(id, owner)
                .orElseThrow(() -> new NotFoundException("Pièce jointe introuvable: " + id));
    }

    public record DownloadedFile(String filename, String contentType, InputStream stream) {
    }
}
