package com.example.batalla_naval.Model;
import java.io.Serializable;

public interface ShootStrategy extends Serializable {
    int[] chooseTarget(Board enemyBoard);
}