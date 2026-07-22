package com.example.batalla_naval.Model;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.Deque;

public class GameState implements Serializable {
    private HumanP humanPlayer;
    private MachineP machinePlayer;
    private boolean end;
    private String currentTurnPlayerName;
    private Deque<Movement> history;

    public GameState(HumanP humanPlayer, MachineP machinePlayer, boolean end,
                     String currentTurnPlayerName, Deque<Movement> history){
        this.humanPlayer = humanPlayer;
        this.machinePlayer = machinePlayer;
        this.end = end;
        this.currentTurnPlayerName = currentTurnPlayerName;
        this.history = new ArrayDeque<>(history);
    }

    public HumanP getHumanPlayer(){ return humanPlayer; }
    public MachineP getMachinePlayer(){ return machinePlayer; }
    public boolean isEnd(){ return end; }
    public String getCurrentTurnPlayerName(){ return currentTurnPlayerName; }
    public Deque<Movement> getHistory(){ return history; }
}