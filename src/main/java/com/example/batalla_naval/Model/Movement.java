package com.example.batalla_naval.Model;

public class Movement {
    private int row;
    private int column;
    private CStatus result;
    private String playerName;

    public Movement(int row, int column, CStatus result, String playerName){
        this.row = row;
        this.column = column;
        this.result = result;
        this.playerName = playerName;
    }

    public int getRow(){ return row; }
    public int getColumn(){ return column; }
    public CStatus getResult(){ return result; }
    public String getPlayerName(){ return playerName; }
}