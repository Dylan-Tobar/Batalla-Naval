package com.example.batalla_naval.Model;

public class Player {
    private String name;
    private Board board;
    private int sunkSCount;

    public Player(String name){
        this.name = name;
        this.board = new Board();
        this.sunkSCount = 0;
    }

    public void addSunkS(){
        sunkSCount += 1;
    }

    public String getName(){
        return name;
    }

    public Board getBoard(){
        return board;
    }

    public int getSunkSCount(){
        return sunkSCount;
    }

}