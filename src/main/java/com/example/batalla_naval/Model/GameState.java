package com.example.batalla_naval.Model;

import java.io.Serializable;

public class GameState implements Serializable {
    private Board hBoard;
    private Board mBoard;
    private boolean end;

    public GameState(Board humanBoard, Board machineBoard, boolean end){
        this.hBoard = humanBoard;
        this.mBoard = machineBoard;
        this.end = end;
    }

    public Board gethBoard(){ return hBoard; }
    public Board getmBoard(){ return mBoard; }
    public boolean isEnd(){ return end; }
}