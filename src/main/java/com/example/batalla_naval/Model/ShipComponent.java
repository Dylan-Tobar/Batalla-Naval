package com.example.batalla_naval.Model;
import java.io.Serializable;

public interface ShipComponent extends Serializable {
    boolean isSunk();
    int getSize();
}