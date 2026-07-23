package com.example.batalla_naval.Model;

/**
 * @autor Dylan Tobar, Ricardo Hallado, Alejandro Arias
 * @version 1.0
 * Unchecked exception thrown when the player tries to shoot a cell
 * that has already been shot before.
 */
public class AlreadyShotException extends RuntimeException {

    /**
     * Creates the exception with a message describing the error.
     * @param message text explaining why the shot is invalid
     */
    public AlreadyShotException(String message){
        super(message);
    }
}
