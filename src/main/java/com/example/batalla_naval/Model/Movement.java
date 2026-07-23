package com.example.batalla_naval.Model;

import java.io.Serializable;

/**
 * @autor Dylan Tobar, Ricardo Hallado, Alejandro Arias
 * @version 1.0
 * Represents a single move made during the match, used to
 * build the game history log.
 */
public class Movement implements Serializable {
    private int row;
    private int column;
    private CStatus result;
    private String playerName;

    /**
     * Creates a record of a shot that was made.
     * @param row row of the shot
     * @param column column of the shot
     * @param result outcome of the shot (water, touched or drowned)
     * @param playerName name of the player who shot
     */
    public Movement(int row, int column, CStatus result, String playerName){
        this.row = row;
        this.column = column;
        this.result = result;
        this.playerName = playerName;
    }

    /**
     * Returns the row of this shot.
     * @return row index
     */
    public int getRow(){ return row; }

    /**
     * Returns the column of this shot.
     * @return column index
     */
    public int getColumn(){ return column; }

    /**
     * Returns the outcome of this shot.
     * @return the shot result
     */
    public CStatus getResult(){ return result; }

    /**
     * Returns the name of the player who made this shot.
     * @return player name
     */
    public String getPlayerName(){ return playerName; }
}
