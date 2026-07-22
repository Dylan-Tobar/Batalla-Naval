package com.example.batalla_naval.Model;
import java.io.Serializable;
import java.util.ArrayList;

public class Ship implements ShipComponent, Serializable {
    private int size;
    private Orientation or;
    private ShipType type;
    private ArrayList<Cell> cells;
    private int hitCount = 0;

    public Ship(int size, Orientation or, ShipType type){
        this.size = size;
        this.or = or;
        this.type = type;
        this.cells = new ArrayList<>();
    }

    public void addCell(Cell cell){
        cells.add(cell);
    }

    public void hit(){
        hitCount += 1;
    }

    @Override
    public boolean isSunk(){
        if(size == hitCount){
            return true;
        } else{
            return false;
        }
    }

    @Override
    public int getSize(){
        return size;
    }

    public ShipType getType(){
        return type;
    }

    public int getHitCount(){
        return hitCount;
    }

    public Orientation getOr(){
        return or;
    }

    public ArrayList<Cell> getCells(){
        return cells;
    }

}
