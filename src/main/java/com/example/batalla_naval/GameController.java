package com.example.batalla_naval;

import com.example.batalla_naval.Model.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

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

    @FXML
    public void initialize() {
        // Inicializar ComboBox con tipos de barco
        shipTypeComboBox.getItems().addAll(ShipType.values());
        shipTypeComboBox.setValue(ShipType.AIRCRAFT_CARRIER);

        // Formato para mostrar nombres legibles en ComboBox
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

        // Configurar arrastre (Drag Source HU-1)
        setupDragSource();

        // Construir cuadrículas de tableros
        buildGrids();

        // Configurar botones de control
        btnStartGame.setOnAction(e -> handleStartGame());
        btnShowOpponent.setOnAction(e -> handleToggleShowOpponent());
        btnNewGame.setOnAction(e -> handleNewGame());

        // Cargar o iniciar nueva partida al arrancar (HU-6)
        Platform.runLater(this::checkLoadSavedGame);
    }

    private void setupDragSource() {
        dragShipSource.setOnDragDetected(event -> {
            if (gameStarted) return;
            ShipType selectedType = shipTypeComboBox.getValue();
            if (selectedType == null) return;

            // Verificar si aún se pueden colocar más barcos de este tipo
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

    private void buildGrids() {
        humanGrid.getChildren().clear();
        machineGrid.getChildren().clear();

        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 10; c++) {
                final int row = r;
                final int col = c;

                // Celda Humano
                Button hBtn = new Button();
                hBtn.getStyleClass().add("grid-cell");
                setupHumanCellDragAndDrop(hBtn, row, col);
                // También permitir clic directo para comodidad del usuario
                hBtn.setOnAction(e -> handleHumanCellClick(row, col));
                humanButtons[r][c] = hBtn;
                humanGrid.add(hBtn, c, r);

                // Celda Máquina
                Button mBtn = new Button();
                mBtn.getStyleClass().add("grid-cell");
                mBtn.setOnAction(e -> handleMachineCellClick(row, col));
                machineButtons[r][c] = mBtn;
                machineGrid.add(mBtn, c, r);
            }
        }
    }

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

    private void handleHumanCellClick(int row, int col) {
        if (gameStarted) return;
        ShipType selectedType = shipTypeComboBox.getValue();
        if (selectedType == null) return;
        Orientation selectedOr = radioHorizontal.isSelected() ? Orientation.HORIZONTAL : Orientation.VERTICAL;
        attemptPlaceShip(selectedType, selectedOr, row, col);
    }

    private boolean attemptPlaceShip(ShipType type, Orientation orientation, int row, int col) {
        try {
            Ship ship = ShipFactory.createShip(type, orientation);
            game.placeHS(ship, row, col);

            statusLabel.setText("✅ " + getShipTypeDisplayName(type) + " colocado en (" + row + ", " + col + ")");
            updateFleetCounts();
            renderHumanBoard();

            // Si la flota está completa (10 barcos)
            if (game.getHumaP().getBoard().isReady()) {
                btnStartGame.setDisable(false);
                statusLabel.setText("🎉 ¡Flota completa! Presiona 'Iniciar Batalla' para comenzar.");
            }
            return true;
        } catch (InvPosException | OccupiedCellException e) {
            statusLabel.setText("❌ Error de colocación: " + e.getMessage());
            return false;
        }
    }

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
            // OJO: humanShot recibe (columna, fila) -> (col, row)
            CStatus result = game.humanShot(col, row);
            String actionDesc = "Humano disparó a (" + row + ", " + col + "): " + getStatusText(result);
            logListView.getItems().add(0, actionDesc);

            renderMachineBoard();
            checkGameEnd();

            // Si el juego continúa y le toca a la máquina, ejecutar turno automático
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

    private void executeMachineTurn() {
        if (game.isEnd()) return;

        Platform.runLater(() -> {
            try {
                CStatus resultM = game.machineShot();
                logListView.getItems().add(0, "Máquina disparó: " + getStatusText(resultM));
                renderHumanBoard();
                checkGameEnd();

                if (!game.isEnd() && game.getcTurn() == game.getmPlayer()) {
                    // Si la máquina acertó, vuelve a disparar
                    executeMachineTurn();
                } else {
                    turnLabel.setText("Turno: " + game.getcTurn().getName());
                }
            } catch (Exception e) {
                System.out.println("Error en turno de la máquina: " + e.getMessage());
            }
        });
    }

    private void handleStartGame() {
        if (!game.getHumaP().getBoard().isReady()) {
            statusLabel.setText("⚠️ Coloca toda tu flota antes de iniciar.");
            return;
        }

        // Colocar barcos de la máquina de forma aleatoria
        game.placeMShipsR();
        gameStarted = true;

        btnStartGame.setDisable(true);
        btnShowOpponent.setVisible(false); // HU-3: Ocultar botón tras iniciar
        turnLabel.setText("Turno: " + game.getcTurn().getName());
        statusLabel.setText("⚔ ¡La batalla ha comenzado! Haz clic en el tablero enemigo para disparar.");
    }

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

    private void startFreshGame() {
        game = new Game("Jugador 1", "Máquina");
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

    private void restoreSavedGameState() {
        winnerOverlay.setVisible(false);
        logListView.getItems().clear();

        // Restaurar historial
        List<Movement> hist = game.getHistory();
        for (int i = hist.size() - 1; i >= 0; i--) {
            Movement m = hist.get(i);
            logListView.getItems().add(0, m.getPlayerName() + " disparó a (" + m.getRow() + ", " + m.getCol() + "): " + getStatusText(m.getResult()));
        }

        // Si la flota humana ya estaba completa al guardar
        if (game.getHumaP().getBoard().isReady()) {
            gameStarted = true;
            btnStartGame.setDisable(true);
            btnShowOpponent.setVisible(false);
        } else {
            gameStarted = false;
            btnStartGame.setDisable(!game.getHumaP().getBoard().isReady());
            btnShowOpponent.setVisible(true);
        }

        updateFleetCounts();
        renderHumanBoard();
        renderMachineBoard();
        checkGameEnd();
    }

    private void handleNewGame() {
        startFreshGame();
    }

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

    private void updateFleetCounts() {
        Board humanBoard = game.getHumaP().getBoard();

        carrierCountLabel.setText("Portaaviones (4 celdas): " + countPlaced(humanBoard, ShipType.AIRCRAFT_CARRIER) + "/1");
        submarineCountLabel.setText("Submarinos (3 celdas): " + countPlaced(humanBoard, ShipType.SUBMARINE) + "/2");
        destroyerCountLabel.setText("Destructores (2 celdas): " + countPlaced(humanBoard, ShipType.DESTROYER) + "/3");
        frigateCountLabel.setText("Fragatas (1 celda): " + countPlaced(humanBoard, ShipType.FRIGATE) + "/4");
    }

    private int countPlaced(Board board, ShipType type) {
        int count = 0;
        for (Ship s : board.getFleet().getShips()) {
            if (s.getType() == type) {
                count++;
            }
        }
        return count;
    }

    private String getShipTypeDisplayName(ShipType type) {
        switch (type) {
            case AIRCRAFT_CARRIER: return "Portaaviones (4 celdas)";
            case SUBMARINE: return "Submarino (3 celdas)";
            case DESTROYER: return "Destructor (2 celdas)";
            case FRIGATE: return "Fragata (1 celda)";
            default: return type.name();
        }
    }

    private String getStatusText(CStatus status) {
        switch (status) {
            case WATER: return "Agua";
            case TOUCHED: return "Tocado";
            case DROWNED: return "¡HUNDIDO!";
            default: return status.name();
        }
    }
}
