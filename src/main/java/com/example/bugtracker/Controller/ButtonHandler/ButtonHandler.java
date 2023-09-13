package com.example.bugtracker.Controller.ButtonHandler;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class ButtonHandler implements EventHandler<ActionEvent> {
    private final String fxmlPath;

    public ButtonHandler(String fxmlPath) {
        this.fxmlPath = fxmlPath;
    }

    @Override
    public void handle(ActionEvent event) {
        Stage currentStage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        currentStage.close();

        try {
            Parent root = FXMLLoader.load((Objects.requireNonNull(getClass().getResource(fxmlPath))));

            // Create a new stage and set it to the loaded FXML
            Stage newStage = new Stage();
            newStage.setScene(new Scene(root));

            // Show the new stage
            newStage.show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}



