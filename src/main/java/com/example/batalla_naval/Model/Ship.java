package com.example.batalla_naval.Model;
import java.io.Serializable;
import java.util.ArrayList;

public class Ship implements ShipComponent {
    private int size;
    private Orientation or;
    private ArrayList<Cell> cells;
    private int hitCount = 0;

    public Ship(int size, Orientation or){
        this.size = size;
        this.or = or;
        this.cells = new ArrayList<>();
    }

    public void addCell(Cell cell){
        cells.add(cell);
    }

    public void hit(){
        hitCount += 1;
    }

    public boolean isSunk(){
        if(size == hitCount){
            return true;
        } else{
            return false;
        }
    }

    public int getSize(){
        return size;
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
