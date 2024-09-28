package ics432.imgapp;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import javafx.application.Platform;

public class StatisticsWindow extends Stage {
    private final Stage primaryStage;
    private final Button closeButton;

    /**
     * Constructor
     *
     * @param primaryStage The primary stage
     */
    StatisticsWindow(Stage primaryStage, int windowWidth, int windowHeight) {

        double buttonPreferredHeight = 27.0;

        // Set up the primaryStage
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Statistics");

        closeButton = new Button("Close");
        closeButton.setPrefHeight(buttonPreferredHeight);
        closeButton.setOnAction(e -> this.primaryStage.close()); // Close primaryStage here

        // Create the layout
        VBox layout = new VBox();
        layout.setSpacing(5);

        HBox row = new HBox();
        row.setSpacing(5);

        // Add the quit button
        row.getChildren().add(closeButton);

        layout.getChildren().add(row);

        // Create the scene
        Scene scene = new Scene(layout, windowWidth, windowHeight);

        // Set the scene
        this.primaryStage.setScene(scene);

        // Show the primaryStage
        this.primaryStage.show();
    }

    /**
     * Close the window
     */

    public void addCloseListener(Runnable listener) {
        this.primaryStage.addEventHandler(WindowEvent.WINDOW_HIDDEN, (event) -> listener.run()); // Add listener to
                                                                                                 // primaryStage
    }
}
