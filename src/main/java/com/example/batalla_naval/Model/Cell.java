package com.example.batalla_naval.Model;
import java.io.Serializable;

/**
 * @autor Dylan Tobar, Ricardo Hallado, Alejandro Arias
 * @version 1.0
 * A single cell of the 10x10 board.
 */
public class Cell implements Serializable{
    private int row, column;
    private CStatus status;
    private Ship ship;

    /**
     * Creates a new empty cell at the given position.
     * @param row row index (0 to 9)
     * @param column column index (0 to 9)
     */
    public Cell(int row, int column){
        this.row = row;
        this.column = column;
        this.status = CStatus.VOID;
    }

    /**
     * Updates the status of this cell.
     * @param status new status to set
     */
    public void setStatus(CStatus status) {
        this.status = status;
    }

    /**
     * Returns the current status of this cell.
     * @return the cell status
     */
    public CStatus getStatus() {
        return status;
    }

    /**
     * Returns the ship placed on this cell, or null if empty.
     * @return the ship on this cell, or null
     */
    public Ship getShip() {
        return ship;
    }

    /**
     * Assigns a ship to this cell.
     * @param ship the ship occupying this cell
     */
    public void setShip(Ship ship){
        this.ship = ship;
    }
}
