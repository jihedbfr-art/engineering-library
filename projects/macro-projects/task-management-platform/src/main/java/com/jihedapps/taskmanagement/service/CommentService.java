package com.jihedapps.taskmanagement.service;

import com.jihedapps.taskmanagement.entity.Comment;
import com.jihedapps.taskmanagement.entity.Task;
import com.jihedapps.taskmanagement.entity.User;
import com.jihedapps.taskmanagement.repository.CommentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserService userService;
    private final TaskService taskService;

    public CommentService(CommentRepository commentRepository, UserService userService, TaskService taskService) {
        this.commentRepository = commentRepository;
        this.userService = userService;
        this.taskService = taskService;
    }

    @Transactional
    public Comment addComment(Long requesterId, Long taskId, String body) {
        User author = userService.requireById(requesterId);
        Task task = taskService.requireById(taskId);
        Comment comment = new Comment(task, author, body);
        return commentRepository.save(comment);
    }

    @Transactional(readOnly = true)
    public List<Comment> listByTask(Long taskId) {
        return commentRepository.findByTaskIdOrderByCreatedAtAsc(taskId);
    }
}
