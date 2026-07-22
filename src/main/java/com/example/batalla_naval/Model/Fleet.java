package com.example.batalla_naval.Model;
import java.io.Serializable;
import java.util.ArrayList;

public class Fleet implements ShipComponent{
    private ArrayList<Ship> ships;

    public Fleet(){
        this.ships = new ArrayList<>();
    }

    public boolean isSunk(){
        for(int i = 0; i < ships.size(); i++){
            if(ships.get(i).isSunk() == false){
                return false;
            }
        }
        return true;
    }

    public void addShip(Ship ship){
        ships.add(ship);
    }

    public boolean isDefeated(){
        for(int i = 0; i < ships.size(); i++){
            if(ships.get(i).isSunk() == false){
                return false;
            }
        }
        return true;
    }

    public ArrayList<Ship> getShips(){
        return ships;
    }
}
