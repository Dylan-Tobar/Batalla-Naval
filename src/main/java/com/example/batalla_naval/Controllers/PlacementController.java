package com.example.batalla_naval.Controllers;

import com.example.batalla_naval.Model.Board;
import com.example.batalla_naval.Model.CStatus;
import com.example.batalla_naval.Model.Cell;
import com.example.batalla_naval.Model.Game;
import com.example.batalla_naval.Model.InvPosException;
import com.example.batalla_naval.Model.Orientation;
import com.example.batalla_naval.Model.Ship;
import com.example.batalla_naval.Model.ShipFactory;
import com.example.batalla_naval.Model.ShipType;
import com.example.batalla_naval.Navigator;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

/**
 * @autor Dylan Tobar, Ricardo Hallado, Alejandro Arias
 * @version 1.0
 * Controller for the fleet-placement screen. Covers HU-1 (drag or
 * click to place the 10 human ships, validating overlaps and
 * out-of-bounds positions) and HU-3 (a verification popup that
 * reveals the machine's already-placed fleet, available only
 * before the match starts).
 */
public class PlacementController {

    @FXML private Label statusLabel;
    @FXML private ToggleButton radioHorizontal;
    @FXML private ToggleButton radioVertical;
    @FXML private ToggleGroup orientationGroup;
    @FXML private VBox shipCardsBox;
    @FXML private Button btnStartGame;
    @FXML private Button btnShowOpponent;
    @FXML private GridPane humanGrid;

    private Game game;
    private final Button[][] humanButtons = new Button[10][10];

    /** Orden en que se muestran los tipos de barco en el panel de tarjetas. */
    private static final List<ShipType> SHIP_ORDER = List.of(
            ShipType.AIRCRAFT_CARRIER, ShipType.SUBMARINE, ShipType.DESTROYER, ShipType.FRIGATE);

    /** Tipo actualmente seleccionado (arrastrado o usado para colocar con clic). */
    private ShipType activeType;

    /**
     * Called automatically by JavaFX after the FXML is loaded.
     * Prepares the ship selector, the drag source and the empty
     * board; the actual match arrives afterwards through
     * {@link #setGame(Game)}.
     */
    @FXML
    public void initialize() {
        buildGrid();

        // Al cambiar la orientación, las vistas previas de las tarjetas rotan.
        orientationGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle == null) {
                // Los ToggleButton se pueden "des-seleccionar" con un clic;
                // no se permite quedar sin orientación elegida.
                oldToggle.setSelected(true);
                return;
            }
            refreshCardOrientation();
        });

        // Atajo de teclado: la tecla R rota entre horizontal y vertical.
        shipCardsBox.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.addEventFilter(KeyEvent.KEY_PRESSED, this::handleKeyPress);
            }
        });

        btnStartGame.setOnAction(e -> handleStartGame());
        btnShowOpponent.setOnAction(e -> handleShowOpponentBoard());
    }

    /**
     * Global key listener for this screen: pressing R rotates the
     * ship orientation between horizontal and vertical, as a
     * faster alternative to clicking the toggle buttons.
     * @param event the key event to inspect
     */
    private void handleKeyPress(KeyEvent event) {
        if (event.getCode() == KeyCode.R) {
            // Siempre seleccionamos el toggle "de destino" en vez de
            // deseleccionar el activo: al estar ambos en el mismo
            // ToggleGroup, deseleccionar primero el que está activo
            // (setSelected(false)) deja el grupo sin selección por un
            // instante, y el listener de selectedToggleProperty lo
            // revierte de inmediato (por eso antes solo funcionaba en
            // un sentido). Seleccionar directamente el otro evita ese
            // problema porque el grupo desactiva el anterior por sí solo.
            if (radioHorizontal.isSelected()) {
                radioVertical.setSelected(true);
            } else {
                radioHorizontal.setSelected(true);
            }
        }
    }

    /**
     * Receives the match coming from the welcome screen, whose
     * machine fleet is already placed, and renders the empty human
     * board ready for placement.
     * @param game the match to place ships on
     */
    public void setGame(Game game) {
        this.game = game;
        rebuildShipCards();
        renderHumanBoard();
    }

    /**
     * Rebuilds the ship-selection panel: one draggable card per ship
     * type that still has ships left to place, showing its preview
     * image and remaining count. A type whose quota is complete is
     * simply left out, so the panel moves on to the next type on its
     * own without the user having to reselect anything.
     */
    private void rebuildShipCards() {
        shipCardsBox.getChildren().clear();
        Board humanBoard = game.getHumaP().getBoard();

        List<ShipType> remaining = new ArrayList<>();
        for (ShipType type : SHIP_ORDER) {
            if (humanBoard.canPlaceType(type)) {
                remaining.add(type);
            }
        }

        if (activeType == null || !remaining.contains(activeType)) {
            activeType = remaining.isEmpty() ? null : remaining.get(0);
        }

        for (ShipType type : remaining) {
            shipCardsBox.getChildren().add(buildShipCard(type, humanBoard));
        }
    }

    /**
     * Builds one draggable card for a ship type: preview image,
     * name and how many of that type remain to be placed.
     * @param type ship type the card represents
     * @param humanBoard the human player's board, used for counts
     * @return the card node, ready to add to the panel
     */
    private HBox buildShipCard(ShipType type, Board humanBoard) {
        ImageView preview = new ImageView(new Image(
                getClass().getResourceAsStream(getShipImagePath(type))));
        preview.setFitHeight(78);
        preview.setPreserveRatio(true);
        preview.setId("preview-" + type.name());
        applyOrientationRotation(preview);

        StackPane previewBox = new StackPane(preview);
        previewBox.getStyleClass().add("ship-card-preview-box");

        Label nameLabel = new Label(getShipTypeDisplayName(type));
        nameLabel.getStyleClass().add("ship-card-name");
        nameLabel.setWrapText(true);

        int placed = countPlaced(humanBoard, type);
        Label countLabel = new Label((maxForType(type) - placed) + " de " + maxForType(type) + " por colocar");
        countLabel.getStyleClass().add("ship-card-count");

        VBox textBox = new VBox(4, nameLabel, countLabel);
        textBox.setAlignment(Pos.CENTER_LEFT);

        HBox card = new HBox(14, previewBox, textBox);
        card.setAlignment(Pos.CENTER_LEFT);
        card.getStyleClass().add("ship-card");
        if (type == activeType) {
            card.getStyleClass().add("ship-card-active");
        }

        card.setOnMouseClicked(e -> {
            activeType = type;
            rebuildShipCards();
        });

        card.setOnDragDetected(event -> {
            activeType = type;
            Dragboard db = card.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            Orientation selectedOr = radioHorizontal.isSelected() ? Orientation.HORIZONTAL : Orientation.VERTICAL;
            content.putString(type.name() + ":" + selectedOr.name());
            db.setContent(content);
            event.consume();
        });

        return card;
    }

    /**
     * Rotates a ship preview image to match the currently selected
     * orientation (ships point "up" in their source image, i.e.
     * vertical by default).
     * @param preview the ImageView to rotate
     */
    private void applyOrientationRotation(ImageView preview) {
        preview.setRotate(radioHorizontal.isSelected() ? 90 : 0);
    }

    /**
     * Re-applies the current orientation's rotation to every ship
     * preview already on screen, without rebuilding the whole panel.
     */
    private void refreshCardOrientation() {
        for (javafx.scene.Node node : shipCardsBox.getChildren()) {
            if (node instanceof HBox card) {
                for (javafx.scene.Node child : card.getChildren()) {
                    if (child instanceof StackPane previewBox) {
                        for (javafx.scene.Node inner : previewBox.getChildren()) {
                            if (inner instanceof ImageView iv) {
                                applyOrientationRotation(iv);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Resource path of the preview image for the given ship type.
     * @param type the ship type
     * @return classpath-relative path to its PNG image
     */
    private String getShipImagePath(ShipType type) {
        switch (type) {
            case AIRCRAFT_CARRIER: return "/com/example/batalla_naval/images/ships/carrier.png";
            case SUBMARINE: return "/com/example/batalla_naval/images/ships/submarine.png";
            case DESTROYER: return "/com/example/batalla_naval/images/ships/destroyer.png";
            case FRIGATE: return "/com/example/batalla_naval/images/ships/frigate.png";
            default: return "/com/example/batalla_naval/images/ships/frigate.png";
        }
    }

    /**
     * Maximum number of ships of the given type allowed in a fleet.
     * @param type the ship type
     * @return how many ships of that type a full fleet contains
     */
    private int maxForType(ShipType type) {
        switch (type) {
            case AIRCRAFT_CARRIER: return 1;
            case SUBMARINE: return 2;
            case DESTROYER: return 3;
            case FRIGATE: return 4;
            default: return 0;
        }
    }

    /**
     * Builds the 10x10 grid of buttons for the human board and
     * wires up their click and drag-and-drop handlers.
     */
    private void buildGrid() {
        humanGrid.getChildren().clear();

        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 10; c++) {
                final int row = r;
                final int col = c;

                Button hBtn = new Button();
                hBtn.getStyleClass().add("grid-cell");
                setupCellDragAndDrop(hBtn, row, col);
                hBtn.setOnAction(e -> handleCellClick(row, col));
                humanButtons[r][c] = hBtn;
                humanGrid.add(hBtn, c, r);
            }
        }
    }

    /**
     * Enables a single board cell to accept a dragged ship and
     * attempt to place it when dropped.
     * @param btn the button representing the cell
     * @param row row of this cell
     * @param col column of this cell
     */
    private void setupCellDragAndDrop(Button btn, int row, int col) {
        btn.setOnDragOver(event -> {
            if (event.getDragboard().hasString()) {
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

            if (db.hasString()) {
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
     * Handles a direct click on a board cell as an alternative way
     * to place the currently selected ship.
     * @param row row that was clicked
     * @param col column that was clicked
     */
    private void handleCellClick(int row, int col) {
        if (activeType == null) return;
        Orientation selectedOr = radioHorizontal.isSelected() ? Orientation.HORIZONTAL : Orientation.VERTICAL;
        attemptPlaceShip(activeType, selectedOr, row, col);
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
            rebuildShipCards();
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
     * Handles the "Start Game" button: locks both boards and moves
     * on to the battle screen.
     */
    private void handleStartGame() {
        try {
            game.startGame();
        } catch (IllegalStateException e) {
            statusLabel.setText("⚠️ " + e.getMessage());
            return;
        }
        Navigator.goToBattle(game);
    }

    /**
     * HU-3: opens a read-only popup window showing the machine's
     * fully placed fleet, for verification purposes only. This
     * option only exists on this screen, so it naturally disappears
     * once the match starts.
     */
    private void handleShowOpponentBoard() {
        GridPane grid = new GridPane();
        Cell[][] machineCells = game.getmPlayer().getBoard().getGrid();

        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 10; c++) {
                Label cell = new Label();
                cell.getStyleClass().add("grid-cell");
                if (machineCells[r][c].getStatus() == CStatus.OCCUPIED) {
                    cell.getStyleClass().add("cell-opponent-ship");
                    cell.setText("🚢");
                }
                grid.add(cell, c, r);
            }
        }

        VBox root = new VBox(12,
                new Label("👁 Flota completa de la máquina (solo para verificación)"),
                grid);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-padding: 20px; -fx-background-color: #ffffff;");

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/com/example/batalla_naval/styles.css").toExternalForm());

        Stage popup = new Stage();
        popup.setTitle("Tablero de Posición del Oponente");
        popup.setScene(scene);
        popup.setResizable(false);
        popup.show();
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
     * Redraws every cell of the human board according to whether
     * it currently holds a ship.
     */
    private void renderHumanBoard() {
        Cell[][] grid = game.getHumaP().getBoard().getGrid();
        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 10; c++) {
                Button btn = humanButtons[r][c];
                Cell cell = grid[r][c];

                btn.getStyleClass().remove("cell-ship");

                if (cell.getStatus() == CStatus.OCCUPIED) {
                    btn.getStyleClass().add("cell-ship");
                    btn.setText("🚢");
                } else {
                    btn.setText("");
                }
            }
        }
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
}
