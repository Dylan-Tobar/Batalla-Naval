package com.example.batalla_naval.Model;

/**
 * @autor Dylan Tobar, Ricardo Hallado, Alejandro Arias
 * @version 1.0
 * Checked exception thrown when a ship is placed in an invalid
 * position: outside the board or overlapping another ship.
 */
public class InvPosException extends Exception{
    /**
     * Creates the exception with a message describing the error.
     * @param message text explaining why the position is invalid
     */
    public InvPosException(String message){
        super(message);
    }
}
