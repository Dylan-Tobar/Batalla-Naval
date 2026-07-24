package com.example.batalla_naval.Model;

import java.io.Serializable;
import java.util.Stack;

/**
 * @autor Dylan Tobar, Ricardo Hallado, Alejandro Arias
 * @version 1.0
 * Snapshot of everything needed to save and restore a match:
 * both players, whether it ended or started, whose turn it is,
 * and the move history. This is the object that gets serialized
 * to disk.
 */
public class GameState implements Serializable {
    private HumanP humanPlayer;
    private MachineP machinePlayer;
    private boolean end;
    private boolean started;
    private String cTurnPName;
    private Stack<Movement> history;

    /**
     * Creates a new saved-state snapshot.
     * @param humanPlayer the human player and their board
     * @param machinePlayer the machine player and their board
     * @param end whether the match has finished
     * @param started whether the match has started
     * @param cTurnPName name of the player whose turn it is
     * @param history the move history so far
     */
    public GameState(HumanP humanPlayer, MachineP machinePlayer, boolean end, boolean started,
                     String cTurnPName, Stack<Movement> history){
        this.humanPlayer = humanPlayer;
        this.machinePlayer = machinePlayer;
        this.end = end;
        this.started = started;
        this.cTurnPName = cTurnPName;
        this.history = new Stack<>();
        this.history.addAll(history);
    }

    /**
     * Returns the saved human player.
     * @return the human player
     */
    public HumanP getHumanPlayer(){ return humanPlayer; }

    /**
     * Returns the saved machine player.
     * @return the machine player
     */
    public MachineP getMachinePlayer(){ return machinePlayer; }

    /**
     * Returns whether the saved match had finished.
     * @return true if the match was over
     */
    public boolean isEnd(){ return end; }

    /**
     * Returns whether the saved match had started.
     * @return true if the match had started
     */
    public boolean isStarted(){ return started; }

    /**
     * Returns the name of the player whose turn it was.
     * @return current turn player's name
     */
    public String getcTurnPName(){ return cTurnPName; }

    /**
     * Returns the saved move history.
     * @return the history of moves
     */
    public Stack<Movement> getHistory(){ return history; }
}
