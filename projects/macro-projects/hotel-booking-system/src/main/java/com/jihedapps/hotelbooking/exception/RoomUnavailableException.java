package com.jihedapps.hotelbooking.exception;

/**
 * Levee quand une chambre est demandee sur une periode qui chevauche
 * une reservation active existante.
 */
public class RoomUnavailableException extends RuntimeException {

    public RoomUnavailableException(String message) {
        super(message);
    }
}
