package com.jihedapps.helpdesk.dto;

/**
 * Temps moyen de resolution (en minutes) pour un agent, calcule sur les
 * tickets qui lui sont actuellement assignes et qui ont atteint le statut
 * RESOLVED au moins une fois.
 */
public record ResolutionReport(
        Long agentId,
        String agentUsername,
        long resolvedTicketCount,
        double averageResolutionMinutes) {
}
