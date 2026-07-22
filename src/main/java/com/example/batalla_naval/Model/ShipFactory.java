package com.example.batalla_naval.Model;

public class ShipFactory {

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

        return new Ship(size, or);
    }
}