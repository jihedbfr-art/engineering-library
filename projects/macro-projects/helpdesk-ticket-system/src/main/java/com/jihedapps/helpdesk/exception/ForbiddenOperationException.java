package com.jihedapps.helpdesk.exception;

/**
 * Levee quand un agent tente une operation reservee a un role qu'il n'a pas,
 * par exemple un AGENT qui tente de reassigner un ticket a un autre agent.
 */
public class ForbiddenOperationException extends RuntimeException {

    public ForbiddenOperationException(String message) {
        super(message);
    }
}
