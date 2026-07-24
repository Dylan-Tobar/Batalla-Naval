package com.example.batalla_naval.Model;
import java.io.IOException;
import java.util.*;

/**
 * @autor Dylan Tobar, Ricardo Hallado, Alejandro Arias
 * @version 1.0
 * Orchestrates a full match.
 */
public class Game {
    private HumanP humaP;
    private MachineP mPlayer;
    private Player cTurn;
    private boolean end;
    private boolean started;
    private Stack<Movement> history = new Stack<>();
    private Queue<Player> turnQueue = new LinkedList<>();

    /**
     * Creates a brand new match with both players and an empty
     * history, starting with the human player's turn.
     * @param humanName nickname for the human player
     * @param machineName nickname for the machine player
     */
    public Game(String humanName, String machineName){
        this.humaP = new HumanP(humanName);
        this.mPlayer = new MachineP(machineName);
        turnQueue.add(humaP);
        turnQueue.add(mPlayer);
        this.cTurn = humaP;
        this.end = false;
        this.started = false;
    }

    /**
     * Rebuilds a match from a previously saved state.
     * @param state the saved game state
     */
    private Game(GameState state){
        this.humaP = state.getHumanPlayer();
        this.mPlayer = state.getMachinePlayer();
        this.end = state.isEnd();
        this.started = state.isStarted();
        this.history = new Stack<>();
        this.history.addAll(state.getHistory());

        if(state.getcTurnPName().equals(humaP.getName())){
            turnQueue.add(humaP);
            turnQueue.add(mPlayer);
        } else {
            turnQueue.add(mPlayer);
            turnQueue.add(humaP);
        }
        this.cTurn = turnQueue.peek();
    }

    /**
     * Loads the last saved match if one exists and is not finished,
     * otherwise creates a brand new one.
     * @param humanName nickname to use for a new match
     * @param machineName nickname to use for a new match
     * @return the loaded or newly created game
     */
    public static Game loadOrCreate(String humanName, String machineName){
        try {
            GameState saved = PersistenceService.loadGame();
            if(saved != null && !saved.isEnd()){
                return new Game(saved);
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("No se pudo cargar la partida guardada, se inicia una nueva");
        }
        return new Game(humanName, machineName);
    }

    /** Passes the turn to the next player in the turn queue. */
    public void changeT(){
        Player next = turnQueue.poll();
        turnQueue.add(next);
        cTurn = turnQueue.peek();
    }

    /** Checks both fleets and marks the match as finished if either is defeated. */
    public void checkEnd(){
        if(humaP.getBoard().getFleet().isDefeated() || mPlayer.getBoard().getFleet().isDefeated()){
            end = true;
        }
    }


    /**
     * Returns the winner of the match, if it has finished.
     * @return the winning player, or null if the match is not over
     *         or ended without a defeated fleet
     */
    public Player getWinner(){
        if(!end){
            return null;
        }
        if(humaP.getBoard().getFleet().isDefeated()){
            return mPlayer;
        }
        if(mPlayer.getBoard().getFleet().isDefeated()){
            return humaP;
        }
        return null;
    }

    /**
     * Returns a copy of the full move history, most recent first.
     * @return list of every move made so far
     */
    public List<Movement> getHistory(){
        return new ArrayList<>(history);
    }

    /**
     * Processes a shot made by the human player against the
     * machine's board, updates the turn/history, and auto-saves
     * the match.
     * @param c column to shoot
     * @param r row to shoot
     * @return the result of the shot
     * @throws GameOverException if the match already finished
     * @throws IllegalStateException if it is not the human player's turn
     */
    public CStatus humanShot(int c, int r){
        if(end){
            throw new GameOverException("La partida ya terminó");
        }
        if(cTurn != humaP){
            throw new IllegalStateException("No es el turno del jugador humano");
        }

        CStatus result = mPlayer.getBoard().shoot(r, c);

        if(result == CStatus.DROWNED){
            humaP.addSunkS();
        }

        checkEnd();

        if(result == CStatus.WATER){
            changeT();
        }

        history.push(new Movement(r, c, result, humaP.getName()));

        try{
            PersistenceService.saveGame(new GameState(humaP, mPlayer, end, started, cTurn.getName(), history));
            PersistenceService.saveRecord(humaP.getName(), humaP.getSunkSCount());
        } catch(IOException e){
            System.out.println("Error al guardar la partida");
        }

        return result;
    }

    /**
     * Processes an automatic shot made by the machine player
     * against the human's board, updates its shooting strategy,
     * updates the turn/history, and auto-saves the match.
     * @return the result of the shot
     * @throws GameOverException if the match already finished
     * @throws IllegalStateException if it is not the machine's turn
     */
    public CStatus machineShot(){
        if(end){
            throw new GameOverException("La partida ya terminó");
        }
        if(cTurn != mPlayer){
            throw new IllegalStateException("No es el turno de la máquina");
        }

        int[] target = mPlayer.chooseTarget(humaP.getBoard());
        int r = target[0];
        int c = target[1];

        CStatus resultM = humaP.getBoard().shoot(r, c);

        if(resultM == CStatus.TOUCHED){
            mPlayer.setStrategy(new HunterShot(r, c));
        } else {
            mPlayer.setStrategy(new RandomShot());
        }

        if(resultM == CStatus.DROWNED){
            mPlayer.addSunkS();
        }

        checkEnd();

        if(resultM == CStatus.WATER){
            changeT();
        }

        history.push(new Movement(r, c, resultM, mPlayer.getName()));

        try{
            PersistenceService.saveGame(new GameState(humaP, mPlayer, end, started, cTurn.getName(), history));
            PersistenceService.saveRecord(humaP.getName(), humaP.getSunkSCount());
        } catch(IOException e){
            System.out.println("Error al guardar la partida");
        }

        return resultM;
    }

    /**
     * Places a ship on the human player's board.
     * @param ship the ship to place
     * @param r starting row
     * @param c starting column
     * @throws InvPosException if the position is invalid
     * @throws IllegalStateException if the match has already started
     */
    public void placeHS(Ship ship, int r, int c) throws InvPosException {
        if(started){
            throw new IllegalStateException("La partida ya inició, no puedes colocar más barcos");
        }
        humaP.getBoard().placeShip(ship, r, c);
    }

    /**
     * Starts the match: places the machine's fleet randomly and
     * locks both boards so no more ships can be placed.
     * @throws IllegalStateException if the match already started or
     *         the human fleet is not complete yet
     */
    public void startGame(){
        if(started){
            throw new IllegalStateException("La partida ya fue iniciada");
        }
        if(!humaP.getBoard().isReady()){
            throw new IllegalStateException("Debes completar tu flota antes de iniciar la partida");
        }

        humaP.getBoard().lock();
        mPlayer.getBoard().lock();
        started = true;
    }

    /**
     * Checks if the match has started.
     * @return true if the match has already started
     */
    public boolean isStarted(){
        return started;
    }

    /**
     * Places the machine's full fleet of 10 ships in random valid
     * positions on its own board.
     */
    public void placeMShipsR(){
        ShipType[] types = {
                ShipType.AIRCRAFT_CARRIER,
                ShipType.SUBMARINE, ShipType.SUBMARINE,
                ShipType.DESTROYER, ShipType.DESTROYER, ShipType.DESTROYER,
                ShipType.FRIGATE, ShipType.FRIGATE, ShipType.FRIGATE, ShipType.FRIGATE
        };

        for(int i = 0; i < types.length; i++){
            boolean p = false;

            while(!p){
                int r = (int)(Math.random()*10);
                int c = (int)(Math.random()*10);
                Orientation randomOr = Math.random() < 0.5 ? Orientation.HORIZONTAL : Orientation.VERTICAL;

                Ship ship = ShipFactory.createShip(types[i], randomOr);

                try{
                    mPlayer.getBoard().placeShip(ship, r, c);
                    p = true;
                } catch (InvPosException e){
                }
            }
        }
    }

    /**
     * Returns the human player.
     * @return the human player
     */
    public HumanP getHumaP(){ return humaP; }

    /**
     * Returns the machine player.
     * @return the machine player
     */
    public MachineP getmPlayer(){ return mPlayer; }

    /**
     * Returns the player whose turn it currently is.
     * @return current turn player
     */
    public Player getcTurn(){ return cTurn; }

    /**
     * Checks if the match has finished.
     * @return true if the match is over
     */
    public boolean isEnd(){ return end; }
}
