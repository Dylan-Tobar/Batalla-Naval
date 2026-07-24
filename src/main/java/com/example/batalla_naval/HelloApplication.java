package com.example.batalla_naval;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * @autor Dylan Tobar, Ricardo Hallado, Alejandro Arias
 * @version 1.0
 * Entry point of the application. Prepares the primary stage and
 * hands control over to {@link Navigator}, which starts the flow
 * on the welcome screen.
 */
public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) {
        stage.setTitle("Batalla Naval");
        stage.setResizable(true);
        stage.setMinWidth(950.0);
        stage.setMinHeight(600.0);
        Navigator.setStage(stage);
        Navigator.goToWelcome();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
