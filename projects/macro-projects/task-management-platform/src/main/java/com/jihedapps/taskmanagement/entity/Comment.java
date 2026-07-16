package com.jihedapps.taskmanagement.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "task_comment")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(nullable = false, length = 2000)
    private String body;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    protected Comment() {
    }

    public Comment(Task task, User author, String body) {
        this.task = task;
        this.author = author;
        this.body = body;
    }

    public Long getId() {
        return id;
    }

    public Task getTask() {
        return task;
    }

    public User getAuthor() {
        return author;
    }

    public String getBody() {
        return body;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
