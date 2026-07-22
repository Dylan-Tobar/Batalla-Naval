package com.example.batalla_naval.Model;
import java.io.IOException;
import java.util.*;

public class Game {
    private HumanP humaP;
    private MachineP mPlayer;
    private Player cTurn;
    private boolean end;
    private Deque<Movement> history = new ArrayDeque<>();
    private Queue<Player> turnQueue = new LinkedList<>();

    public Game(String humanName, String machineName){
        this.humaP = new HumanP(humanName);
        this.mPlayer = new MachineP(machineName);
        turnQueue.add(humaP);
        turnQueue.add(mPlayer);
        this.cTurn = humaP;
        this.end = false;
    }

    private Game(GameState state){
        this.humaP = state.getHumanPlayer();
        this.mPlayer = state.getMachinePlayer();
        this.end = state.isEnd();
        this.history = new ArrayDeque<>(state.getHistory());

        if(state.getCurrentTurnPlayerName().equals(humaP.getName())){
            turnQueue.add(humaP);
            turnQueue.add(mPlayer);
        } else {
            turnQueue.add(mPlayer);
            turnQueue.add(humaP);
        }
        this.cTurn = turnQueue.peek();
    }

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

    public void changeT(){
        Player next = turnQueue.poll();
        turnQueue.add(next);
        cTurn = turnQueue.peek();
    }

    public void checkEnd(){
        if(humaP.getBoard().getFleet().isDefeated() || mPlayer.getBoard().getFleet().isDefeated()){
            end = true;
        }
    }


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

    // Punto 9: se expone el history en vez de dejarlo "muerto" (solo push, nunca leído)
    public List<Movement> getHistory(){
        return new ArrayList<>(history);
    }

    public CStatus humanShot(int c, int r){
        if(end){
            throw new GameOverException("La partida ya terminó");
        }
        // Punto 12: enforcement de turno dentro del Model
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
            PersistenceService.saveGame(new GameState(humaP, mPlayer, end, cTurn.getName(), history));
            PersistenceService.saveRecord(humaP.getName(), humaP.getSunkSCount());
        } catch(IOException e){
            System.out.println("Error al guardar la partida");
        }

        return result;
    }

    public CStatus machineShot(){
        if(end){
            throw new GameOverException("La partida ya terminó");
        }
        // Punto 12: enforcement de turno dentro del Model
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
            PersistenceService.saveGame(new GameState(humaP, mPlayer, end, cTurn.getName(), history));
            PersistenceService.saveRecord(humaP.getName(), humaP.getSunkSCount());
        } catch(IOException e){
            System.out.println("Error al guardar la partida");
        }

        return resultM;
    }

    public void placeHS(Ship ship, int r, int c) throws InvPosException {
        humaP.getBoard().placeShip(ship, r, c);
    }

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
                    // no hacemos nada, el while vuelve a intentar con otra posición
                }
            }
        }
    }

    public HumanP getHumaP(){ return humaP; }
    public MachineP getmPlayer(){ return mPlayer; }
    public Player getcTurn(){ return cTurn; }
    public boolean isEnd(){ return end; }
}