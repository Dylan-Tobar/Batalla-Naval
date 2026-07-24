package com.example.batalla_naval.Controllers;

import com.example.batalla_naval.Model.AlreadyShotException;
import com.example.batalla_naval.Model.Board;
import com.example.batalla_naval.Model.CStatus;
import com.example.batalla_naval.Model.Cell;
import com.example.batalla_naval.Model.Game;
import com.example.batalla_naval.Model.GameOverException;
import com.example.batalla_naval.Model.GameState;
import com.example.batalla_naval.Model.InvPosException;
import com.example.batalla_naval.Model.Movement;
import com.example.batalla_naval.Model.OccupiedCellException;
import com.example.batalla_naval.Model.Orientation;
import com.example.batalla_naval.Model.PersistenceService;
import com.example.batalla_naval.Model.Player;
import com.example.batalla_naval.Model.Ship;
import com.example.batalla_naval.Model.ShipFactory;
import com.example.batalla_naval.Model.ShipType;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.util.List;
import java.util.Optional;

/**
 * @autor Dylan Tobar, Ricardo Hallado, Alejandro Arias
 * @version 1.0
 * JavaFX controller for the main game screen. Connects the FXML
 * view with the {@link Game} model: builds the two boards, handles
 * ship placement (drag and drop or click), player and machine
 * shots, and shows the current game status on screen.
 */
public class GameController {

    @FXML private StackPane rootPane;
    @FXML private Label turnLabel;
    @FXML private Label statusLabel;
    @FXML private VBox placementPanel;
    @FXML private ComboBox<ShipType> shipTypeComboBox;
    @FXML private RadioButton radioHorizontal;
    @FXML private RadioButton radioVertical;
    @FXML private ToggleGroup orientationGroup;
    @FXML private Label dragShipSource;
    @FXML private Label carrierCountLabel;
    @FXML private Label submarineCountLabel;
    @FXML private Label destroyerCountLabel;
    @FXML private Label frigateCountLabel;
    @FXML private Button btnStartGame;
    @FXML private Button btnShowOpponent;
    @FXML private GridPane humanGrid;
    @FXML private GridPane machineGrid;
    @FXML private ListView<String> logListView;
    @FXML private VBox winnerOverlay;
    @FXML private Label winnerLabel;
    @FXML private Button btnNewGame;

    private Game game;
    private boolean gameStarted = false;
    private boolean showingOpponentFleet = false;

    private final Button[][] humanButtons = new Button[10][10];
    private final Button[][] machineButtons = new Button[10][10];

    /**
     * Called automatically by JavaFX after the FXML is loaded.
     * Sets up the ship type selector, the drag source, both board
     * grids, the control buttons, and tries to load a saved match.
     */
    @FXML
    public void initialize() {
        shipTypeComboBox.getItems().addAll(ShipType.values());
        shipTypeComboBox.setValue(ShipType.AIRCRAFT_CARRIER);

        shipTypeComboBox.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(ShipType item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(getShipTypeDisplayName(item));
                }
            }
        });
        shipTypeComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(ShipType item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(getShipTypeDisplayName(item));
                }
            }
        });

        setupDragSource();

        buildGrids();

        btnStartGame.setOnAction(e -> handleStartGame());
        btnShowOpponent.setOnAction(e -> handleToggleShowOpponent());
        btnNewGame.setOnAction(e -> handleNewGame());
        Platform.runLater(this::checkLoadSavedGame);
    }

    /**
     * Configures the drag source label so the user can start
     * dragging the currently selected ship type onto their board.
     */
    private void setupDragSource() {
        dragShipSource.setOnDragDetected(event -> {
            if (gameStarted) return;
            ShipType selectedType = shipTypeComboBox.getValue();
            if (selectedType == null) return;

            if (!game.getHumaP().getBoard().canPlaceType(selectedType)) {
                statusLabel.setText("⚠️ Ya colocaste el número máximo de " + getShipTypeDisplayName(selectedType));
                return;
            }

            Dragboard db = dragShipSource.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();

            Orientation selectedOr = radioHorizontal.isSelected() ? Orientation.HORIZONTAL : Orientation.VERTICAL;
            content.putString(selectedType.name() + ":" + selectedOr.name());
            db.setContent(content);
            event.consume();
        });
    }

    /**
     * Builds the two 10x10 grids of buttons (human and machine
     * boards) and wires up their click and drag-and-drop handlers.
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
                setupHumanCellDragAndDrop(hBtn, row, col);
                hBtn.setOnAction(e -> handleHumanCellClick(row, col));
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
     * Enables a single human-board cell to accept a dragged ship
     * and attempt to place it when dropped.
     * @param btn the button representing the cell
     * @param row row of this cell
     * @param col column of this cell
     */
    private void setupHumanCellDragAndDrop(Button btn, int row, int col) {
        btn.setOnDragOver(event -> {
            if (!gameStarted && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
                if (!btn.getStyleClass().contains("cell-drag-over")) {
                    btn.getStyleClass().add("cell-drag-over");
                }
            }
            event.consume();
        });

        btn.setOnDragExited(event -> {
            btn.getStyleClass().remove("cell-drag-over");
            event.consume();
        });

        btn.setOnDragDropped(event -> {
            btn.getStyleClass().remove("cell-drag-over");
            Dragboard db = event.getDragboard();
            boolean success = false;

            if (!gameStarted && db.hasString()) {
                String[] data = db.getString().split(":");
                if (data.length == 2) {
                    ShipType type = ShipType.valueOf(data[0]);
                    Orientation orientation = Orientation.valueOf(data[1]);
                    success = attemptPlaceShip(type, orientation, row, col);
                }
            }
            event.setDropCompleted(success);
            event.consume();
        });
    }

    /**
     * Handles a direct click on a human-board cell as an
     * alternative way to place the currently selected ship.
     * @param row row that was clicked
     * @param col column that was clicked
     */
    private void handleHumanCellClick(int row, int col) {
        if (gameStarted) return;
        ShipType selectedType = shipTypeComboBox.getValue();
        if (selectedType == null) return;
        Orientation selectedOr = radioHorizontal.isSelected() ? Orientation.HORIZONTAL : Orientation.VERTICAL;
        attemptPlaceShip(selectedType, selectedOr, row, col);
    }

    /**
     * Tries to place a ship of the given type and orientation on
     * the human board, updating the UI on success or showing an
     * error message on failure.
     * @param type ship type to place
     * @param orientation orientation to place it in
     * @param row starting row
     * @param col starting column
     * @return true if the ship was placed successfully
     */
    private boolean attemptPlaceShip(ShipType type, Orientation orientation, int row, int col) {
        try {
            Ship ship = ShipFactory.createShip(type, orientation);
            game.placeHS(ship, row, col);

            statusLabel.setText("✅ " + getShipTypeDisplayName(type) + " colocado en (" + row + ", " + col + ")");
            updateFleetCounts();
            renderHumanBoard();

            if (game.getHumaP().getBoard().isReady()) {
                btnStartGame.setDisable(false);
                statusLabel.setText("🎉 ¡Flota completa! Presiona 'Iniciar Batalla' para comenzar.");
            }
            return true;

        } catch (InvPosException e) {
            statusLabel.setText("❌ Error de colocación: " + e.getMessage());
            return false;
        } catch (IllegalStateException e) {

            statusLabel.setText("⚠️ " + e.getMessage());
            return false;
        }
    }

    /**
     * Handles a click on a machine-board cell: fires a shot from
     * the human player if it is their turn and the match is running.
     * @param row row that was clicked
     * @param col column that was clicked
     */
    private void handleMachineCellClick(int row, int col) {
        if (!gameStarted) {
            statusLabel.setText("⚠️ Debes iniciar la partida primero.");
            return;
        }
        if (game.isEnd()) {
            return;
        }
        if (game.getcTurn() != game.getHumaP()) {
            statusLabel.setText("⚠️ No es tu turno.");
            return;
        }

        try {
            CStatus result = game.humanShot(col, row);
            String actionDesc = "Humano disparó a (" + row + ", " + col + "): " + getStatusText(result);
            logListView.getItems().add(0, actionDesc);

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
     * Handles the "Start Game" button: starts the match if the
     * human fleet is complete, locking both boards.
     */
    private void handleStartGame() {
        try {
            game.startGame();
        } catch (IllegalStateException e) {
            statusLabel.setText("⚠️ " + e.getMessage());
            return;
        }

        gameStarted = true;

        btnStartGame.setDisable(true);
        btnShowOpponent.setVisible(false); // HU-3: Ocultar botón tras iniciar
        turnLabel.setText("Turno: " + game.getcTurn().getName());
        statusLabel.setText("⚔ ¡La batalla ha comenzado! Haz clic en el tablero enemigo para disparar.");
    }

    /**
     * Handles the "Show Opponent Board" button: toggles whether the
     * machine's fleet is revealed on screen for testing purposes.
     */
    private void handleToggleShowOpponent() {
        showingOpponentFleet = !showingOpponentFleet;
        if (showingOpponentFleet) {
            btnShowOpponent.setText("🙈 Ocultar Tablero Oponente");
            statusLabel.setText("👁 Mostrando tablero del oponente (Modo Pruebas).");
        } else {
            btnShowOpponent.setText("👁 Ver Tablero Oponente");
            statusLabel.setText("Fase de colocación: Ubica tus 10 barcos en el tablero.");
        }
        renderMachineBoard();
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

            showRecords();
        }
    }

    /**
     *  Read the record history saved in record.txty;
     *  it is displayed in a pop-up window when the game ends.
     */
    private void showRecords() {
        try {
            List<String> records = PersistenceService.loadRecords();

            String content = "";
            for (int i = 0; i < records.size(); i++) {
                content += records.get(i) + "\n";
            }

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Récords");
            alert.setHeaderText("Historial de partidas jugadas");
            alert.setContentText(content.length() > 0 ? content : "Aún no hay récords guardados.");
            alert.showAndWait();

        } catch (Exception e) {
            System.out.println("No se pudo leer los récords: " + e.getMessage());
        }
    }

    /**
     * Checks if there is a saved, unfinished match on disk and, if
     * so, asks the user whether to resume it or start a new one.
     */
    private void checkLoadSavedGame() {
        try {
            GameState saved = PersistenceService.loadGame();
            if (saved != null && !saved.isEnd()) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Cargar Partida");
                alert.setHeaderText("Se encontró una partida guardada de " + saved.getHumanPlayer().getName());
                alert.setContentText("¿Deseas continuar la partida guardada o iniciar una nueva?");

                ButtonType btnLoad = new ButtonType("Cargar Guardada");
                ButtonType btnNew = new ButtonType("Nueva Partida");
                alert.getButtonTypes().setAll(btnLoad, btnNew);

                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == btnLoad) {
                    game = Game.loadOrCreate("Jugador 1", "Máquina");
                    restoreSavedGameState();
                    return;
                }
            }
        } catch (Exception e) {
            System.out.println("No se pudo leer la partida guardada: " + e.getMessage());
        }

        startFreshGame();
    }

    /**
     * Starts a brand new match and resets the whole screen to the
     * initial ship-placement state.
     */
    private void startFreshGame() {
        game = new Game("Jugador 1", "Máquina");
        game.placeMShipsR(); // Se coloca desde ya para que "Ver Tablero Oponente" muestre algo real en Modo Pruebas
        gameStarted = false;
        showingOpponentFleet = false;
        btnStartGame.setDisable(true);
        btnShowOpponent.setVisible(true);
        winnerOverlay.setVisible(false);
        logListView.getItems().clear();

        updateFleetCounts();
        renderHumanBoard();
        renderMachineBoard();
        turnLabel.setText("Fase: Colocación de Flota");
        statusLabel.setText("Arrastra o haz clic para colocar tus 10 barcos.");
    }

    /**
     * Rebuilds the whole screen (log, boards, buttons) to match a
     * match that was just loaded from a saved state.
     */
    private void restoreSavedGameState() {
        winnerOverlay.setVisible(false);
        logListView.getItems().clear();

        List<Movement> hist = game.getHistory();
        for (int i = hist.size() - 1; i >= 0; i--) {
            Movement m = hist.get(i);
            logListView.getItems().add(0, m.getPlayerName() + " disparó a (" + m.getRow() + ", " + m.getColumn() + "): " + getStatusText(m.getResult()));
        }

        gameStarted = game.isStarted();
        if (gameStarted) {
            btnStartGame.setDisable(true);
            btnShowOpponent.setVisible(false);
        } else {
            btnStartGame.setDisable(!game.getHumaP().getBoard().isReady());
            btnShowOpponent.setVisible(true);
        }

        updateFleetCounts();
        renderHumanBoard();
        renderMachineBoard();
        checkGameEnd();
    }

    /** Handles the "New Game" button by starting a fresh match. */
    private void handleNewGame() {
        startFreshGame();
    }

    /**
     * Redraws every cell of the human board according to its
     * current status (empty, ship, water, touched or drowned).
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
     * current status, only revealing hidden ships when the "show
     * opponent" test mode is active.
     */
    private void renderMachineBoard() {
        Cell[][] grid = game.getmPlayer().getBoard().getGrid();
        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 10; c++) {
                Button btn = machineButtons[r][c];
                Cell cell = grid[r][c];

                btn.getStyleClass().removeAll("cell-ship", "cell-opponent-ship", "cell-water", "cell-touched", "cell-sunk");

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
                    case OCCUPIED:
                        if (showingOpponentFleet) {
                            btn.getStyleClass().add("cell-opponent-ship");
                            btn.setText("🚢");
                        } else {
                            btn.setText("");
                        }
                        break;
                    default:
                        btn.setText("");
                        break;
                }
            }
        }
    }

    /**
     * Updates the four labels that show how many ships of each
     * type the human player has placed so far.
     */
    private void updateFleetCounts() {
        Board humanBoard = game.getHumaP().getBoard();

        carrierCountLabel.setText("Portaaviones (4 celdas): " + countPlaced(humanBoard, ShipType.AIRCRAFT_CARRIER) + "/1");
        submarineCountLabel.setText("Submarinos (3 celdas): " + countPlaced(humanBoard, ShipType.SUBMARINE) + "/2");
        destroyerCountLabel.setText("Destructores (2 celdas): " + countPlaced(humanBoard, ShipType.DESTROYER) + "/3");
        frigateCountLabel.setText("Fragatas (1 celda): " + countPlaced(humanBoard, ShipType.FRIGATE) + "/4");
    }

    /**
     * Counts how many ships of the given type are already placed
     * on the given board.
     * @param board the board to inspect
     * @param type the ship type to count
     * @return number of ships of that type placed on the board
     */
    private int countPlaced(Board board, ShipType type) {
        int count = 0;
        for (Ship s : board.getFleet().getShips()) {
            if (s.getType() == type) {
                count++;
            }
        }
        return count;
    }

    /**
     * Returns a human-readable Spanish name (with cell count) for a
     * ship type, used in labels and the ComboBox.
     * @param type the ship type
     * @return display text for that ship type
     */
    private String getShipTypeDisplayName(ShipType type) {
        switch (type) {
            case AIRCRAFT_CARRIER: return "Portaaviones (4 celdas)";
            case SUBMARINE: return "Submarino (3 celdas)";
            case DESTROYER: return "Destructor (2 celdas)";
            case FRIGATE: return "Fragata (1 celda)";
            default: return type.name();
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