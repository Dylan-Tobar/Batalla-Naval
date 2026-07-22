package com.example.batalla_naval.Model;

public class HunterShot implements ShootStrategy {

    private int lastHitR;
    private int lastHitC;

    public HunterShot(int lastHitR, int lastHitC){
        this.lastHitR = lastHitR;
        this.lastHitC = lastHitC;
    }

    public int[] chooseTarget(Board enemyBoard){
        int newR = lastHitR;
        int newC = lastHitC;

        int direction = (int)(Math.random()*4);

        if(direction == 0){
            newR = lastHitR -1;
        } else if(direction == 1){
            newR = lastHitR +1;
        } else if(direction == 2){
            newC = lastHitC -1;
        } else {
            newC = lastHitC +1;
        }

        if(newR < 0 || newR > 9 || newC < 0 || newC > 9){
            newR = lastHitR;
            newC = lastHitC;
        }

        int[] target = new int[2];
        target[0] = newR;
        target[1] = newC;

        return target;
    }
}