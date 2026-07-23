package com.example.batalla_naval.Model;
import java.io.Serializable;

/**
 * @autor Dylan Tobar, Ricardo Hallado, Alejandro Arias
 * @version 1.0
 * Strategy design pattern: contract for any algorithm the machine
 * player can use to decide where to shoot next.
 */
public interface ShootStrategy extends Serializable {

    /**
     * Chooses the next cell to shoot at on the enemy board.
     * @param enemyBoard the board being attacked
     * @return an array with two values: {row, column}
     */
    int[] chooseTarget(Board enemyBoard);
}
