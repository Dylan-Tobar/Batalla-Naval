package com.example.batalla_naval.Model;

public class AlreadyShotException extends RuntimeException {

    public AlreadyShotException(String message){
        super(message);
    }
}