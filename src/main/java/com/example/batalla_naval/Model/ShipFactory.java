package com.example.batalla_naval.Model;

/**
 * @autor Dylan Tobar, Ricardo Hallado, Alejandro Arias
 * @version 1.0
 * Factory Method design pattern: creates instances
 * with the correct size for each, so the rest of
 * the code never has to remember the size rules by hand.
 */
public class ShipFactory {

    /**
     * Creates a new ship of the given type and orientation, with
     * its size already set according to the ship type.
     * @param type the type of ship to create
     * @param or orientation of the new ship
     * @return a new Ship instance
     */
    public static Ship createShip(ShipType type, Orientation or){
        int size;

        switch(type){
            case AIRCRAFT_CARRIER:
                size = 4;
                break;
            case SUBMARINE:
                size = 3;
                break;
            case DESTROYER:
                size = 2;
                break;
            case FRIGATE:
                size = 1;
                break;
            default:
                size = 1;
                break;
        }

        return new Ship(size, or, type);
    }
}
