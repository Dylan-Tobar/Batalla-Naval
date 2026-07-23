package com.example.batalla_naval.Model;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @autor Dylan Tobar, Ricardo Hallado, Alejandro Arias
 * @version 1.0
 * Handles saving and loading game data to disk.
 */
public class PersistenceService {

    /**
     * Serializes and saves the current match state to partida.dat.
     * @param state the match state to save
     * @throws IOException if the file cannot be written
     */
    public static void saveGame(GameState state) throws IOException {
        try (FileOutputStream fileOut = new FileOutputStream("partida.dat");
             ObjectOutputStream out = new ObjectOutputStream(fileOut)) {
            out.writeObject(state);
        }
    }

    /**
     * Loads the last saved match state from partida.dat.
     * @return the saved state, or null if no save file exists
     * @throws IOException if the file cannot be read
     * @throws ClassNotFoundException if the saved object type is unknown
     */
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

    /**
     * Appends a plain-text record with the player's nickname and
     * how many ships they sunk to record.txt.
     * @param nickname the player's nickname
     * @param sunkShips number of ships sunk by that player
     * @throws IOException if the file cannot be written
     */
    public static void saveRecord(String nickname, int sunkShips) throws IOException {
        try (FileWriter writer = new FileWriter("record.txt", true)) {
            writer.write(nickname + "," + sunkShips + System.lineSeparator());
        }
    }

    /**
     * Reads every saved record from record.txt.
     * @return a list of raw "nickname,sunkShips" text lines
     * @throws IOException if the file cannot be read
     */
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
