package com.example.batalla_naval.Model;

public class RandomShot implements ShootStrategy {

    public int[] chooseTarget(Board enemyBoard){
        int f = (int)(Math.random()*10);
        int c = (int)(Math.random()*10);

        int[] target = new int[2];
        target[0] = f;
        target[1] = c;

        return target;
    }
}