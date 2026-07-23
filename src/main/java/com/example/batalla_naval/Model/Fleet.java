package com.example.batalla_naval.Model;
import java.util.ArrayList;

/**
 * @autor Dylan Tobar, Ricardo Hallado, Alejandro Arias
 * @version 1.0
 * Represents a player's full fleet of ships.
 */
public class Fleet implements ShipComponent {
    private ArrayList<Ship> ships;

    /** Creates a new, empty fleet. */
    public Fleet(){
        this.ships = new ArrayList<>();
    }

    /**
     * Checks if every ship in the fleet is sunk.
     * @return true if all ships are sunk
     */
    @Override
    public boolean isSunk(){
        for(Ship ship : ships){
            if(!ship.isSunk()){
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if the fleet has been fully defeated.
     * @return true if all ships are sunk (same as isSunk())
     */
    public boolean isDefeated(){
        return isSunk();
    }

    /**
     * Returns the total number of cells occupied by all ships.
     * @return sum of every ship's size
     */
    @Override
    public int getSize(){
        int total = 0;
        for(Ship ship : ships){
            total += ship.getSize();
        }
        return total;
    }

    /**
     * Adds a ship to this fleet.
     * @param ship the ship to add
     */
    public void addShip(Ship ship){
        ships.add(ship);
    }

    /**
     * Returns the list of ships in this fleet.
     * @return all ships belonging to this fleet
     */
    public ArrayList<Ship> getShips(){
        return ships;
    }
}
