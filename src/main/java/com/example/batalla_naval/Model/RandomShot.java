package com.example.batalla_naval.Model;

/**
 * @autor Dylan Tobar, Ricardo Hallado, Alejandro Arias
 * @version 1.0
 * Shooting strategy that picks a random cell that has not been
 * shot before.
 */
public class RandomShot implements ShootStrategy {

    /**
     * Picks a random, not-yet-shot cell on the enemy board.
     * @param enemyBoard the board being attacked
     * @return an array with two values: {row, column}
     */
    @Override
    public int[] chooseTarget(Board enemyBoard){
        int f, c;
        do {
            f = (int)(Math.random()*10);
            c = (int)(Math.random()*10);
        } while(enemyBoard.isShot(f, c));

        return new int[]{ f, c };
    }
}
