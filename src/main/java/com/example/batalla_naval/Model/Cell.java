package com.example.batalla_naval.Model;
import java.io.Serializable;

public class Cell implements Serializable{
    private int row, column;
    private CStatus status;
    private Ship ship;

    public Cell(int row, int column){
        this.row = row;
        this.column = column;
        this.status = CStatus.VOID;
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }

    public void setStatus(CStatus status) {
        this.status = status;
    }

    public CStatus getStatus() {
        return status;
    }

    public Ship getShip() {
        return ship;
    }

    public void setShip(Ship ship){
        this.ship = ship;
    }
}
