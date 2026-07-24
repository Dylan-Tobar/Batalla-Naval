package com.example.batalla_naval;

import com.example.batalla_naval.Controllers.BattleController;
import com.example.batalla_naval.Controllers.PlacementController;
import com.example.batalla_naval.Model.Game;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * @autor Dylan Tobar, Ricardo Hallado, Alejandro Arias
 * @version 1.0
 * Small helper in charge of switching between the three screens of
 * the application (welcome, placement and battle) and handing the
 * current {@link Game} instance over to whichever controller needs
 * it, so the game state travels with the player instead of living
 * inside a single monolithic controller.
 */
public final class Navigator {

    private static Stage stage;

    private Navigator() {
    }

    /**
     * Stores the primary stage so every screen can be shown on it.
     * @param primaryStage the application's main stage
     */
    static void setStage(Stage primaryStage) {
        stage = primaryStage;
    }

    /** Shows the welcome screen (HU-6: new game vs. resume saved game). */
    public static void goToWelcome() {
        display(load("welcome-view.fxml"));
    }

    /**
     * Shows the fleet-placement screen (HU-1 and HU-3).
     * @param game the match the player is about to place ships on
     */
    public static void goToPlacement(Game game) {
        FXMLLoader loader = load("placement-view.fxml");
        PlacementController controller = loader.getController();
        controller.setGame(game);
        display(loader);
    }

    /**
     * Shows the battle screen (HU-2 and HU-4), fresh or restored.
     * @param game the match currently in progress
     */
    public static void goToBattle(Game game) {
        FXMLLoader loader = load("battle-view.fxml");
        BattleController controller = loader.getController();
        controller.setGame(game);
        display(loader);
    }

    private static FXMLLoader load(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(Navigator.class.getResource(fxml));
            loader.load();
            return loader;
        } catch (IOException e) {
            throw new RuntimeException("No se pudo cargar la vista: " + fxml, e);
        }
    }

    private static void display(FXMLLoader loader) {
        Parent root = loader.getRoot();
        if (stage.getScene() == null) {
            stage.setScene(new Scene(root, 1150, 700));
        } else {
            stage.getScene().setRoot(root);
        }
        stage.show();
    }
}
