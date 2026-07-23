package com.example.batalla_naval.Model;
import java.util.HashMap;
import java.util.Map;
import java.io.Serializable;

/**
 * @autor Dylan Tobar, Ricardo Hallado, Alejandro Arias
 * @version 1.0
 * Represents a player's 10x10 board.
 */
public class Board implements Serializable {
    private Cell[][] grid;
    private Fleet fleet;
    private Map<ShipType, Integer> shipCount = new HashMap<>();
    private boolean locked = false;

    /** Creates a new empty 10x10 board with an empty fleet. */
    public Board(){
        this.fleet = new Fleet();
        this.grid = new Cell[10][10];

        for(int r=0;r<10;r++){
            for(int c =0;c<10;c++){
                grid[r][c] = new Cell(r, c);
            }
        }
    }

    /**
     * Checks if another ship of the given type can still be placed,
     * based on the fleet composition rules.
     * @param type the ship type to check
     * @return true if the limit for that type has not been reached
     */
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

    /**
     * Increases the placed-ship counter for the given type.
     * @param type the ship type just placed
     */
    public void registerShipType(ShipType type){
        int current = shipCount.getOrDefault(type, 0);
        shipCount.put(type, current + 1);
    }

    /** Locks the board so no more ships can be placed on it. */
    public void lock(){
        this.locked = true;
    }

    /**
     * Checks if the board is locked.
     * @return true if the board no longer accepts new ships
     */
    public boolean isLocked(){
        return locked;
    }

    /**
     * Places a ship on the board starting at the given position.
     * Validates that every cell the ship would occupy is inside the
     * board and free before actually placing it.
     * @param ship the ship to place
     * @param initialR starting row
     * @param initialC starting column
     * @return true if the ship was placed successfully
     * @throws InvPosException if the position is invalid, occupied,
     *         or the ship type limit was already reached
     */
    public boolean placeShip(Ship ship, int initialR, int initialC) throws InvPosException {
        if(locked){
            throw new IllegalStateException("El tablero está bloqueado: la partida ya inició, no se pueden colocar más barcos");
        }

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

    /**
     * Checks if the fleet is complete.
     * @return true if the fleet is ready to start the game
     */
    public boolean isReady(){

        return shipCount.getOrDefault(ShipType.AIRCRAFT_CARRIER,0) == 1 &&
                shipCount.getOrDefault(ShipType.SUBMARINE,0) == 2 &&
                shipCount.getOrDefault(ShipType.DESTROYER,0) == 3 &&
                shipCount.getOrDefault(ShipType.FRIGATE,0) == 4;

    }

    /**
     * Processes a shot at the given cell.
     * @param r row to shoot
     * @param c column to shoot
     * @return the resulting status of the shot
     * @throws AlreadyShotException if that cell was already shot before
     */
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

    /**
     * Checks if the given cell has already been shot.
     * @param r row to check
     * @param c column to check
     * @return true if the cell is water, touched or drowned
     */
    public boolean isShot(int r, int c){
        CStatus status = grid[r][c].getStatus();
        return status == CStatus.WATER || status == CStatus.TOUCHED || status == CStatus.DROWNED;
    }

    /**
     * Returns the full 10x10 grid of cells.
     * @return the board grid
     */
    public Cell[][] getGrid(){
        return grid;
    }

    /**
     * Returns the fleet placed on this board.
     * @return the board's fleet
     */
    public Fleet getFleet(){
        return fleet;
    }
}
