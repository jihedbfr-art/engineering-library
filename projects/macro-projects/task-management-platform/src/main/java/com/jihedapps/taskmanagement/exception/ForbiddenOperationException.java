package com.jihedapps.taskmanagement.exception;

/**
 * Levee quand un utilisateur tente une operation que son role ou sa relation
 * avec la ressource ne lui permet pas (ex: MEMBER qui modifie une tache qui
 * ne lui est pas assignee, ou qui tente de creer un projet).
 */
public class ForbiddenOperationException extends RuntimeException {

    public ForbiddenOperationException(String message) {
        super(message);
    }
}
