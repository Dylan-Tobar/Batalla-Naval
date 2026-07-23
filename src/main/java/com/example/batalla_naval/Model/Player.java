package com.example.batalla_naval.Model;

import java.io.Serializable;

/**
 * @autor Dylan Tobar, Ricardo Hallado, Alejandro Arias
 * @version 1.0
 * Base class for a player.
 */
public class Player implements Serializable {
    private String name;
    private Board board;
    private int sunkSCount;

    /**
     * Creates a new player with an empty board.
     * @param name the player's nickname
     */
    public Player(String name){
        this.name = name;
        this.board = new Board();
        this.sunkSCount = 0;
    }

    /** Increases this player's sunk-ships counter by one. */
    public void addSunkS(){
        sunkSCount += 1;
    }

    /**
     * Returns this player's nickname.
     * @return player name
     */
    public String getName(){
        return name;
    }

    /**
     * Returns this player's board.
     * @return the player's board
     */
    public Board getBoard(){
        return board;
    }

    /**
     * Returns how many enemy ships this player has sunk.
     * @return number of ships sunk by this player
     */
    public int getSunkSCount(){
        return sunkSCount;
    }

}
