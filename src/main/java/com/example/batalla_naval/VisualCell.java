package com.example.batalla_naval;

import com.example.batalla_naval.Model.CStatus;
import javafx.scene.control.Button;

public class VisualCell extends Button {
    private final int row;
    private final int col;
    private final boolean isHumanBoard;

    public VisualCell(int row, int col, boolean isHumanBoard) {
        this.row = row;
        this.col = col;
        this.isHumanBoard = isHumanBoard;

        setPrefSize(36, 36);
        setMinSize(36, 36);
        setMaxSize(36, 36);
        updateVisual(CStatus.VOID);
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public void updateVisual(CStatus status) {
        getStyleClass().clear();
        setStyle(""); // reset inline style

        StringBuilder style = new StringBuilder();
        style.append("-fx-border-color: #3b82f6; -fx-border-width: 0.5px; -fx-font-weight: bold; -fx-font-size: 12px; ");

        if (status == null) status = CStatus.VOID;

        switch (status) {
            case VOID:
                style.append("-fx-background-color: #e0f2fe; -fx-text-fill: #0369a1;");
                setText("");
                break;
            case OCCUPIED:
                if (isHumanBoard) {
                    style.append("-fx-background-color: #64748b; -fx-text-fill: white;");
                    setText("🚢");
                } else {
                    // Hide machine ships until hit
                    style.append("-fx-background-color: #e0f2fe;");
                    setText("");
                }
                break;
            case WATER:
                style.append("-fx-background-color: #38bdf8; -fx-text-fill: white;");
                setText("💧");
                break;
            case TOUCHED:
                style.append("-fx-background-color: #ef4444; -fx-text-fill: white;");
                setText("💥");
                break;
            case DROWNED:
                style.append("-fx-background-color: #7f1d1d; -fx-text-fill: white;");
                setText("☠");
                break;
        }

        setStyle(style.toString());
    }
}
