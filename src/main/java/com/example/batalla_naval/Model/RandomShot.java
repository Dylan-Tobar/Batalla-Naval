package com.example.batalla_naval.Model;

public class RandomShot implements ShootStrategy {

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