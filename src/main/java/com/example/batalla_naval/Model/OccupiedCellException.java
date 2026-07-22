package com.example.batalla_naval.Model;

public class OccupiedCellException extends RuntimeException {

    public OccupiedCellException(String message){
        super(message);
    }
}