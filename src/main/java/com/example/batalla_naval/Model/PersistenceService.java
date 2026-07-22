package com.example.batalla_naval.Model;

import java.io.*;

public class PersistenceService {

    public static void saveGame(GameState state) throws IOException {
        FileOutputStream fileOut = new FileOutputStream("partida.dat");
        ObjectOutputStream out = new ObjectOutputStream(fileOut);
        out.writeObject(state);
        out.close();
        fileOut.close();
    }

    public static GameState loadGame() throws IOException, ClassNotFoundException {
        FileInputStream fileIn = new FileInputStream("partida.dat");
        ObjectInputStream in = new ObjectInputStream(fileIn);
        GameState state = (GameState) in.readObject();
        in.close();
        fileIn.close();
        return state;
    }

    public static void saveRecord(String nickname, int sunkShips) throws IOException {
        FileWriter writer = new FileWriter("record.txt");
        writer.write(nickname + "," + sunkShips);
        writer.close();
    }

    public static String loadRecord() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader("record.txt"));
        String line = reader.readLine();
        reader.close();
        return line;
    }
}