package com.example.batalla_naval.Model;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * @autor Dylan Tobar, Ricardo Hallado, Alejandro Arias
 * @version 1.0
 * Represents a single ship: its size, orientation, type, the cells
 * it occupies, and how many of those cells have been hit.
 */
public class Ship implements ShipComponent, Serializable {
    private int size;
    private Orientation or;
    private ShipType type;
    private ArrayList<Cell> cells;
    private int hitCount = 0;

    /**
     * Creates a new ship that has not been placed on the board yet.
     * @param size number of cells this ship occupies
     * @param or orientation of the ship
     * @param type type of ship (used by the factory and the UI)
     */
    public Ship(int size, Orientation or, ShipType type){
        this.size = size;
        this.or = or;
        this.type = type;
        this.cells = new ArrayList<>();
    }

    /**
     * Adds a cell that this ship occupies on the board.
     * @param cell the cell to add
     */
    public void addCell(Cell cell){
        cells.add(cell);
    }

    /** Registers a hit on this ship, increasing its hit counter. */
    public void hit(){
        hitCount += 1;
    }

    /**
     * Checks if every cell of this ship has been hit.
     * @return true if the ship is fully sunk
     */
    @Override
    public boolean isSunk(){
        if(size == hitCount){
            return true;
        } else{
            return false;
        }
    }

    /**
     * Returns the size (number of cells) of this ship.
     * @return ship size
     */
    @Override
    public int getSize(){
        return size;
    }

    /**
     * Returns the type of this ship.
     * @return ship type
     */
    public ShipType getType(){
        return type;
    }

    /**
     * Returns how many cells of this ship have been hit so far.
     * @return current hit count
     */
    public int getHitCount(){
        return hitCount;
    }

    /**
     * Returns the orientation of this ship.
     * @return HORIZONTAL or VERTICAL
     */
    public Orientation getOr(){
        return or;
    }

    /**
     * Returns the list of cells occupied by this ship.
     * @return cells occupied by this ship
     */
    public ArrayList<Cell> getCells(){
        return cells;
    }

}
