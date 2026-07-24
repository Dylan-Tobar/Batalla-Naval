package com.example.batalla_naval.Controllers;

import com.example.batalla_naval.Model.Game;
import com.example.batalla_naval.Model.GameState;
import com.example.batalla_naval.Model.PersistenceService;
import com.example.batalla_naval.Navigator;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/**
 * @autor Dylan Tobar, Ricardo Hallado, Alejandro Arias
 * @version 1.0
 * Controller for the welcome screen. Covers HU-6: lets the player
 * type a nickname and choose between resuming the last saved,
 * unfinished match or starting a brand new one from ship placement.
 */
public class WelcomeController {

    @FXML private TextField nicknameField;
    @FXML private Label savedInfoLabel;
    @FXML private Label errorLabel;
    @FXML private Button btnNewGame;
    @FXML private Button btnContinueGame;

    private GameState savedState;

    /**
     * Called automatically by JavaFX after the FXML is loaded.
     * Checks for a saved match and wires up both buttons.
     */
    @FXML
    public void initialize() {
        checkSavedGame();
        btnNewGame.setOnAction(e -> handleNewGame());
        btnContinueGame.setOnAction(e -> handleContinueGame());
    }

    /**
     * Looks for a saved, unfinished match on disk and, if one
     * exists, reveals the "continue" option with a short summary.
     * If the last saved match already ended, a new game is forced,
     * as required by HU-6.
     */
    private void checkSavedGame() {
        try {
            savedState = PersistenceService.loadGame();
        } catch (Exception e) {
            savedState = null;
        }

        if (savedState != null && !savedState.isEnd()) {
            savedInfoLabel.setText("📁 Se encontró una partida guardada de \"" + savedState.getHumanPlayer().getName()
                    + "\" (" + savedState.getHumanPlayer().getSunkSCount() + " barcos hundidos al oponente).");
            savedInfoLabel.setVisible(true);
            savedInfoLabel.setManaged(true);
            btnContinueGame.setVisible(true);
            btnContinueGame.setManaged(true);
            nicknameField.setText(savedState.getHumanPlayer().getName());
        } else {
            savedState = null;
        }
    }

    /**
     * Handles "Nueva Partida": creates a fresh match with the
     * typed nickname, places the machine's fleet randomly right
     * away, and moves on to the placement screen (HU-1).
     */
    private void handleNewGame() {
        String nickname = (nicknameField.getText() == null || nicknameField.getText().isBlank())
                ? "Jugador 1" : nicknameField.getText().trim();

        Game game = new Game(nickname, "Máquina");
        game.placeMShipsR();
        Navigator.goToPlacement(game);
    }

    /**
     * Handles "Continuar Partida Guardada": restores the saved
     * match (fleets, history and turn) and jumps straight into the
     * battle screen, skipping placement entirely.
     */
    private void handleContinueGame() {
        if (savedState == null) {
            errorLabel.setText("No hay ninguna partida guardada disponible.");
            return;
        }
        Game game = Game.loadOrCreate(savedState.getHumanPlayer().getName(), "Máquina");
        Navigator.goToBattle(game);
    }
}
