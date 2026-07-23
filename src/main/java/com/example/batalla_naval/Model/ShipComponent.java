package com.example.batalla_naval.Model;
import java.io.Serializable;

/**
 * @autor Dylan Tobar, Ricardo Hallado, Alejandro Arias
 * @version 1.0
 * Common contract shared by a single ship and a full fleet.
 */
public interface ShipComponent extends Serializable {

    /**
     * Checks if this component (a ship or a whole fleet) is sunk.
     * @return true if fully sunk
     */
    boolean isSunk();

    /**
     * Returns the total number of cells occupied by this component.
     * @return total occupied cells
     */
    int getSize();
}
