package com.example.batalla_naval.Model;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Queue;

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

    public CStatus humanShot(int c, int r){
        if(end){
            throw new GameOverException("La partida ya terminó");
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
            PersistenceService.saveGame(new GameState(humaP.getBoard(), mPlayer.getBoard(), end));
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
            PersistenceService.saveGame(new GameState(humaP.getBoard(), mPlayer.getBoard(), end));
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
                Orientation randomOr;

                if(Math.random() < 0.5){
                    randomOr = Orientation.HORIZONTAL;
                } else {
                    randomOr = Orientation.VERTICAL;
                }

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

    public HumanP getHumaP(){
        return humaP;
    }

    public MachineP getmPlayer(){
        return mPlayer;
    }

    public Player getcTurn(){
        return cTurn;
    }

    public boolean isEnd(){
        return end;
    }


}