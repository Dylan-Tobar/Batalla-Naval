package com.example.batalla_naval.Model;

/**
 * @autor Dylan Tobar, Ricardo Hallado, Alejandro Arias
 * @version 1.0
 * Unchecked exception thrown when a game action
 * is attempted after the match has already finished.
 */
public class GameOverException extends RuntimeException {
    /**
     * Creates the exception with a message describing the error.
     * @param message text explaining why the action is invalid
     */
    public GameOverException(String message){
        super(message);
    }
}
