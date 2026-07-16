package com.jihedapps.helpdesk.exception;

/**
 * Levee quand une transition de statut de ticket viole le workflow sequentiel
 * strict (OPEN -> IN_PROGRESS -> RESOLVED -> CLOSED, sans saut ni retour en
 * arriere).
 */
public class InvalidStatusTransitionException extends RuntimeException {

    public InvalidStatusTransitionException(String message) {
        super(message);
    }
}
