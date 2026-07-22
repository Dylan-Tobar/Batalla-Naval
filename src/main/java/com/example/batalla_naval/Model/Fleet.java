package com.example.batalla_naval.Model;
import java.util.ArrayList;

public class Fleet implements ShipComponent {
    private ArrayList<Ship> ships;

    public Fleet(){
        this.ships = new ArrayList<>();
    }

    @Override
    public boolean isSunk(){
        for(Ship ship : ships){
            if(!ship.isSunk()){
                return false;
            }
        }
        return true;
    }

    public boolean isDefeated(){
        return isSunk();
    }

    @Override
    public int getSize(){
        int total = 0;
        for(Ship ship : ships){
            total += ship.getSize();
        }
        return total;
    }

    public void addShip(Ship ship){
        ships.add(ship);
    }

    public ArrayList<Ship> getShips(){
        return ships;
    }
}