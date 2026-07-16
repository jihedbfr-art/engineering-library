package com.jihedapps.helpdesk.entity;

/**
 * Workflow strict et sequentiel : chaque statut ne peut avancer que vers le
 * suivant dans cet ordre (pas de saut, ex. OPEN -> CLOSED est interdit sans
 * passer par IN_PROGRESS puis RESOLVED). Voir TicketService.checkTransition.
 */
public enum TicketStatus {
    OPEN,
    IN_PROGRESS,
    RESOLVED,
    CLOSED
}
