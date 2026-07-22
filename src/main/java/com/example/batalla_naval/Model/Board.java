package com.example.batalla_naval.Model;
import java.util.HashMap;
import java.util.Map;
import java.io.Serializable;

public class Board implements Serializable {
    private Cell[][] grid;
    private Fleet fleet;
    private Map<ShipType, Integer> shipCount = new HashMap<>();

    public Board(){
        this.fleet = new Fleet();
        this.grid = new Cell[10][10];

        for(int r=0;r<10;r++){
            for(int c =0;c<10;c++){
                grid[r][c] = new Cell(r, c);
            }
        }
    }

    public boolean canPlaceType(ShipType type){
        int max;
        switch(type){
            case AIRCRAFT_CARRIER: max = 1; break;
            case SUBMARINE: max = 2; break;
            case DESTROYER: max = 3; break;
            case FRIGATE: max = 4; break;
            default: max = 0; break;
        }

        int current = shipCount.getOrDefault(type, 0);
        return current < max;
    }

    public void registerShipType(ShipType type){
        int current = shipCount.getOrDefault(type, 0);
        shipCount.put(type, current + 1);
    }

    public boolean placeShip(Ship ship, int initialR, int initialC) throws InvPosException {
        if(!canPlaceType(ship.getType())){
            throw new InvPosException("Ya se alcanzó el máximo de barcos de tipo " + ship.getType());
        }

        for(int i=0;i<ship.getSize();i++){
            int targetR = initialR;
            int targetC = initialC;

            if(ship.getOr() == Orientation.HORIZONTAL){
                targetC = initialC + i;
            } else if(ship.getOr() == Orientation.VERTICAL){
                targetR = initialR + i;
            }

            if(targetR<0 || targetR>9 || targetC<0 || targetC>9){
                throw new InvPosException("La posición se sale del tablero");
            }

            if(grid[targetR][targetC].getShip() != null){
                throw new OccupiedCellException("Ya hay un barco en la celda (" + targetR + "," + targetC + ")");
            }
        }

        for(int i = 0; i < ship.getSize(); i++){
            int targetR2 = initialR;
            int targetC2 = initialC;

            if(ship.getOr() == Orientation.HORIZONTAL){
                targetC2 = initialC + i;
            } else if(ship.getOr() == Orientation.VERTICAL){
                targetR2 = initialR + i;
            }

            grid[targetR2][targetC2].setShip(ship);
            grid[targetR2][targetC2].setStatus(CStatus.OCCUPIED);
            ship.addCell(grid[targetR2][targetC2]);
        }

        fleet.addShip(ship);
        registerShipType(ship.getType());

        return true;
    }

    public boolean isReady(){

        return shipCount.getOrDefault(ShipType.AIRCRAFT_CARRIER,0) == 1 &&
                shipCount.getOrDefault(ShipType.SUBMARINE,0) == 2 &&
                shipCount.getOrDefault(ShipType.DESTROYER,0) == 3 &&
                shipCount.getOrDefault(ShipType.FRIGATE,0) == 4;

    }

    public CStatus shoot(int r, int c) {

        if (isShot(r, c)) {
            throw new AlreadyShotException("La celda (" + r + "," + c + ") ya fue disparada");
        }

        Cell cell = grid[r][c];

        if (cell.getShip() == null) {
            cell.setStatus(CStatus.WATER);
            return CStatus.WATER;
        }

        Ship ship = cell.getShip();
        ship.hit();

        if (ship.isSunk()) {
            for (Cell cShip : ship.getCells()) {
                cShip.setStatus(CStatus.DROWNED);
            }
            return CStatus.DROWNED;
        } else {
            cell.setStatus(CStatus.TOUCHED);
            return CStatus.TOUCHED;
        }
    }

    public boolean isShot(int r, int c){
        CStatus status = grid[r][c].getStatus();
        return status == CStatus.WATER || status == CStatus.TOUCHED || status == CStatus.DROWNED;
    }

    public Cell[][] getGrid(){
        return grid;
    }

    public Fleet getFleet(){
        return fleet;
    }
}