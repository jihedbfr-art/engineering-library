package com.jihedapps.taskmanagement.controller;

import com.jihedapps.taskmanagement.dto.CommentDtos.CommentResponse;
import com.jihedapps.taskmanagement.dto.CommentDtos.CreateCommentRequest;
import com.jihedapps.taskmanagement.entity.Comment;
import com.jihedapps.taskmanagement.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks/{taskId}/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommentResponse addComment(@RequestHeader("X-User-Id") Long requesterId,
                                       @PathVariable Long taskId,
                                       @Valid @RequestBody CreateCommentRequest request) {
        Comment comment = commentService.addComment(requesterId, taskId, request.body());
        return CommentResponse.from(comment);
    }

    @GetMapping
    public List<CommentResponse> listComments(@PathVariable Long taskId) {
        return commentService.listByTask(taskId).stream().map(CommentResponse::from).toList();
    }
}
