package com.jihedapps.taskmanagement.dto;

import com.jihedapps.taskmanagement.entity.Comment;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

public class CommentDtos {

    public record CreateCommentRequest(@NotBlank String body) {
    }

    public record CommentResponse(Long id, Long taskId, String author, String body, LocalDateTime createdAt) {
        public static CommentResponse from(Comment comment) {
            return new CommentResponse(
                    comment.getId(),
                    comment.getTask().getId(),
                    comment.getAuthor().getUsername(),
                    comment.getBody(),
                    comment.getCreatedAt());
        }
    }
}
