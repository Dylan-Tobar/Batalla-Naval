package com.example.batalla_naval.Model;

/**
 * @autor Dylan Tobar, Ricardo Hallado, Alejandro Arias
 * @version 1.0
 * Checked exception thrown when a ship is placed on a cell that
 * is already occupied by another ship.
 */
public class OccupiedCellException extends InvPosException {

    /**
     * Creates the exception with a message describing the error.
     * @param message text explaining which cell is already occupied
     */
    public OccupiedCellException(String message){
        super(message);
    }
}
