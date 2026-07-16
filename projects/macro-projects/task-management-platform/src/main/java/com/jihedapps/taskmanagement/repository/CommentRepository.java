package com.jihedapps.taskmanagement.repository;

import com.jihedapps.taskmanagement.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByTaskIdOrderByCreatedAtAsc(Long taskId);
}
