package com.example.batalla_naval.Model;

/**
 * @autor Dylan Tobar, Ricardo Hallado, Alejandro Arias
 * @version 1.0
 * Shooting strategy used after the machine hits a ship.
 */
public class HunterShot implements ShootStrategy {

    private int lastHitR;
    private int lastHitC;

    /**
     * Creates a hunter strategy based on the last cell that was hit.
     * @param lastHitR row of the last hit
     * @param lastHitC column of the last hit
     */
    public HunterShot(int lastHitR, int lastHitC){
        this.lastHitR = lastHitR;
        this.lastHitC = lastHitC;
    }

    /**
     * Chooses one of the four cells adjacent to the last hit that
     * is still inside the board and has not been shot yet. If none
     * qualify, it falls back to {@link RandomShot}.
     * @param enemyBoard the board being attacked
     * @return an array with two values: {row, column}
     */
    @Override
    public int[] chooseTarget(Board enemyBoard){
        int[][] directions = {
                { lastHitR - 1, lastHitC },
                { lastHitR + 1, lastHitC },
                { lastHitR, lastHitC - 1 },
                { lastHitR, lastHitC + 1 }
        };

        for(int[] dir : directions){
            int newR = dir[0];
            int newC = dir[1];
            boolean inBounds = newR >= 0 && newR <= 9 && newC >= 0 && newC <= 9;
            if(inBounds && !enemyBoard.isShot(newR, newC)){
                return new int[]{ newR, newC };
            }
        }

        return new RandomShot().chooseTarget(enemyBoard);
    }
}
