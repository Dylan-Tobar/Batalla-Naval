package com.example.batalla_naval.Model;

public class MachineP extends Player {

    private ShootStrategy currentStrategy;

    public MachineP(String name){
        super(name);
        this.currentStrategy = new RandomShot();
    }

    public int[] chooseTarget(Board enemyBoard){
        return currentStrategy.chooseTarget(enemyBoard);
    }

    public void setStrategy(ShootStrategy strategy){
        this.currentStrategy = strategy;
    }
}