package com.example.batalla_naval.Model;

/**
 * @autor Dylan Tobar, Ricardo Hallado, Alejandro Arias
 * @version 1.0
 * Represents the machine player.
 */
public class MachineP extends Player {

    private ShootStrategy currentStrategy;

    /**
     * Creates a new machine player, starting with a random shooting
     * strategy.
     * @param name the machine player's nickname
     */
    public MachineP(String name){
        super(name);
        this.currentStrategy = new RandomShot();
    }

    /**
     * Asks the current strategy to choose the next cell to shoot.
     * @param enemyBoard the board being attacked
     * @return an array with two values: {row, column}
     */
    public int[] chooseTarget(Board enemyBoard){
        return currentStrategy.chooseTarget(enemyBoard);
    }

    /**
     * Changes the shooting strategy used by this machine player.
     * @param strategy the new strategy to use
     */
    public void setStrategy(ShootStrategy strategy){
        this.currentStrategy = strategy;
    }
}
