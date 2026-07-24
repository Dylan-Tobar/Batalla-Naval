package com.example.batalla_naval.Controllers;

import com.example.batalla_naval.Model.AlreadyShotException;
import com.example.batalla_naval.Model.CStatus;
import com.example.batalla_naval.Model.Cell;
import com.example.batalla_naval.Model.Game;
import com.example.batalla_naval.Model.GameOverException;
import com.example.batalla_naval.Model.Movement;
import com.example.batalla_naval.Model.Player;
import com.example.batalla_naval.Navigator;

import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.List;

/**
 * @autor Dylan Tobar, Ricardo Hallado, Alejandro Arias
 * @version 1.0
 * Controller for the battle screen. Covers HU-2 (the human player
 * shoots at the machine's board) and HU-4 (the machine plays back
 * automatically, alternating between random and "hunter" shots).
 * Also restores the move log when arriving from a saved match.
 */
public class BattleController {

    @FXML private Label turnLabel;
    @FXML private Label statusLabel;
    @FXML private GridPane humanGrid;
    @FXML private GridPane machineGrid;
    @FXML private ListView<String> logListView;
    @FXML private VBox winnerOverlay;
    @FXML private Label winnerLabel;
    @FXML private Button btnNewGame;

    private Game game;

    private final Button[][] humanButtons = new Button[10][10];
    private final Button[][] machineButtons = new Button[10][10];

    /**
     * Called automatically by JavaFX after the FXML is loaded.
     * Builds both board grids; the actual match arrives afterwards
     * through {@link #setGame(Game)}.
     */
    @FXML
    public void initialize() {
        buildGrids();
        btnNewGame.setOnAction(e -> Navigator.goToWelcome());
    }

    /**
     * Receives the already-started match (fresh from placement, or
     * restored from a saved file) and renders the initial state of
     * the battle, resuming the machine's turn automatically if it
     * is the one who should play next.
     * @param game the match currently in progress
     */
    public void setGame(Game game) {
        this.game = game;

        restoreLogFromHistory();
        renderHumanBoard();
        renderMachineBoard();

        if (game.isEnd()) {
            checkGameEnd();
            return;
        }

        turnLabel.setText("Turno: " + game.getcTurn().getName());
        if (game.getcTurn() == game.getHumaP()) {
            statusLabel.setText("⚔ Tu turno: haz clic en el tablero enemigo para disparar.");
        } else {
            executeMachineTurn();
        }
    }

    /**
     * Replays the saved move history (if any) into the log list, so
     * a restored match shows everything that happened before.
     */
    private void restoreLogFromHistory() {
        logListView.getItems().clear();
        List<Movement> hist = game.getHistory();
        for (int i = hist.size() - 1; i >= 0; i--) {
            Movement m = hist.get(i);
            logListView.getItems().add(0, m.getPlayerName() + " disparó a (" + m.getRow() + ", " + m.getColumn() + "): " + getStatusText(m.getResult()));
        }
    }

    /**
     * Builds the two 10x10 grids of buttons (human and machine
     * boards). Only the machine board reacts to clicks; the human
     * board is observation-only, as required by the specification.
     */
    private void buildGrids() {
        humanGrid.getChildren().clear();
        machineGrid.getChildren().clear();

        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 10; c++) {
                final int row = r;
                final int col = c;

                Button hBtn = new Button();
                hBtn.getStyleClass().add("grid-cell");
                humanButtons[r][c] = hBtn;
                humanGrid.add(hBtn, c, r);

                Button mBtn = new Button();
                mBtn.getStyleClass().add("grid-cell");
                mBtn.setOnAction(e -> handleMachineCellClick(row, col));
                machineButtons[r][c] = mBtn;
                machineGrid.add(mBtn, c, r);
            }
        }
    }

    /**
     * Handles a click on a machine-board cell: fires a shot from
     * the human player if it is their turn and the match is running.
     * @param row row that was clicked
     * @param col column that was clicked
     */
    private void handleMachineCellClick(int row, int col) {
        if (game == null || game.isEnd()) {
            return;
        }
        if (game.getcTurn() != game.getHumaP()) {
            statusLabel.setText("⚠️ No es tu turno.");
            return;
        }

        try {
            CStatus result = game.humanShot(col, row);
            logListView.getItems().add(0, "Humano disparó a (" + row + ", " + col + "): " + getStatusText(result));

            renderMachineBoard();
            checkGameEnd();

            if (!game.isEnd() && game.getcTurn() == game.getmPlayer()) {
                executeMachineTurn();
            }

        } catch (AlreadyShotException e) {
            statusLabel.setText("⚠️ Ya disparaste a la celda (" + row + ", " + col + ").");
        } catch (GameOverException e) {
            statusLabel.setText("🏁 La partida ha terminado.");
        } catch (Exception e) {
            statusLabel.setText("❌ Error en disparo: " + e.getMessage());
        }
    }

    /**
     * Executes the machine player's turn asynchronously, and keeps
     * shooting again automatically while it keeps hitting ships.
     */
    private void executeMachineTurn() {
        if (game.isEnd()) return;

        statusLabel.setText("🤖 La máquina está disparando...");

        PauseTransition pause = new PauseTransition(Duration.millis(600));
        pause.setOnFinished(e -> {
            try {
                CStatus resultM = game.machineShot();
                logListView.getItems().add(0, "Máquina disparó: " + getStatusText(resultM));
                renderHumanBoard();
                checkGameEnd();

                if (!game.isEnd() && game.getcTurn() == game.getmPlayer()) {
                    executeMachineTurn();
                } else {
                    turnLabel.setText("Turno: " + game.getcTurn().getName());
                    if (!game.isEnd()) {
                        statusLabel.setText("Tu turno: haz clic en el tablero enemigo para disparar.");
                    }
                }
            } catch (Exception ex) {
                System.out.println("Error en turno de la máquina: " + ex.getMessage());
            }
        });
        pause.play();
    }

    /**
     * Redraws every cell of the human board according to its
     * current status (own ship, water, touched or drowned).
     */
    private void renderHumanBoard() {
        Cell[][] grid = game.getHumaP().getBoard().getGrid();
        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 10; c++) {
                Button btn = humanButtons[r][c];
                Cell cell = grid[r][c];

                btn.getStyleClass().removeAll("cell-ship", "cell-water", "cell-touched", "cell-sunk");

                switch (cell.getStatus()) {
                    case OCCUPIED:
                        btn.getStyleClass().add("cell-ship");
                        btn.setText("🚢");
                        break;
                    case WATER:
                        btn.getStyleClass().add("cell-water");
                        btn.setText("•");
                        break;
                    case TOUCHED:
                        btn.getStyleClass().add("cell-touched");
                        btn.setText("💥");
                        break;
                    case DROWNED:
                        btn.getStyleClass().add("cell-sunk");
                        btn.setText("☠");
                        break;
                    default:
                        btn.setText("");
                        break;
                }
            }
        }
    }

    /**
     * Redraws every cell of the machine board according to its
     * current status. Enemy ships that have not been shot yet stay
     * hidden here, unlike the pre-game verification popup.
     */
    private void renderMachineBoard() {
        Cell[][] grid = game.getmPlayer().getBoard().getGrid();
        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 10; c++) {
                Button btn = machineButtons[r][c];
                Cell cell = grid[r][c];

                btn.getStyleClass().removeAll("cell-water", "cell-touched", "cell-sunk");

                switch (cell.getStatus()) {
                    case WATER:
                        btn.getStyleClass().add("cell-water");
                        btn.setText("•");
                        break;
                    case TOUCHED:
                        btn.getStyleClass().add("cell-touched");
                        btn.setText("💥");
                        break;
                    case DROWNED:
                        btn.getStyleClass().add("cell-sunk");
                        btn.setText("☠");
                        break;
                    default:
                        btn.setText("");
                        break;
                }
            }
        }
    }

    /**
     * Updates the turn label and, if the match has finished, shows
     * the winner overlay with the result.
     */
    private void checkGameEnd() {
        turnLabel.setText("Turno: " + game.getcTurn().getName());
        if (game.isEnd()) {
            Player winner = game.getWinner();
            String winnerText = (winner != null) ? "¡Ganador: " + winner.getName() + "!" : "Empate";
            winnerLabel.setText(winnerText);
            winnerOverlay.setVisible(true);
            statusLabel.setText("🏁 " + winnerText);
        }
    }

    /**
     * Returns a human-readable Spanish label for a shot result,
     * used in the status label and the move log.
     * @param status the shot result
     * @return display text for that result
     */
    private String getStatusText(CStatus status) {
        switch (status) {
            case WATER: return "Agua";
            case TOUCHED: return "Tocado";
            case DROWNED: return "¡HUNDIDO!";
            default: return status.name();
        }
    }
}
