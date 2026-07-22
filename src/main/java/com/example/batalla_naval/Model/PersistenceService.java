package com.example.batalla_naval.Model;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class PersistenceService {

    public static void saveGame(GameState state) throws IOException {
        try (FileOutputStream fileOut = new FileOutputStream("partida.dat");
             ObjectOutputStream out = new ObjectOutputStream(fileOut)) {
            out.writeObject(state);
        }
    }

    public static GameState loadGame() throws IOException, ClassNotFoundException {
        File file = new File("partida.dat");
        if (!file.exists()) {
            return null;
        }
        try (FileInputStream fileIn = new FileInputStream(file);
             ObjectInputStream in = new ObjectInputStream(fileIn)) {
            return (GameState) in.readObject();
        }
    }

    public static void saveRecord(String nickname, int sunkShips) throws IOException {
        try (FileWriter writer = new FileWriter("record.txt", true)) {
            writer.write(nickname + "," + sunkShips + System.lineSeparator());
        }
    }

    public static List<String> loadRecords() throws IOException {
        List<String> records = new ArrayList<>();
        File file = new File("record.txt");
        if (!file.exists()) {
            return records;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                records.add(line);
            }
        }
        return records;
    }
}