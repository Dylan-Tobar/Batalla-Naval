package com.example.batalla_naval.Model;

public interface ShootStrategy {
    int[] chooseTarget(Board enemyBoard);
}