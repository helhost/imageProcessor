package ics432.imgapp;

import javafx.collections.FXCollections;
import javafx.event.Event;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javafx.application.Platform;


/**
 * A class that implements a "Job Window" on which a user
 * can launch a Job
 */

class JobWindow extends Stage {

    private Path targetDir;
    private final List<Path> inputFiles;
    private final FileListWithViewPort flwvp;
    private final Button changeDirButton;
    private final TextField targetDirTextField;
    private final Button runButton;
    private final Button closeButton;
    private final Button cancelButton;
    private final ComboBox<String> imgTransformList;

    // Label used to display metrics
    private Label metricsLabel;

    /**
     * Constructor
     *
     * @param windowWidth  The window's width
     * @param windowHeight The window's height
     * @param X            The horizontal position of the job window
     * @param Y            The vertical position of the job window
     * @param id           The id of the job
     * @param inputFiles   The batch of input image files
     */
    JobWindow(int windowWidth, int windowHeight, double X, double Y, int id, List<Path> inputFiles) {

        // The  preferred height of buttons
        double buttonPreferredHeight = 27.0;

        // Set up instance variables
        targetDir = Paths.get(inputFiles.getFirst().getParent().toString()); // Same dir as input images
        this.inputFiles = inputFiles;


        // Set up the window
        this.setX(X);
        this.setY(Y);
        this.setTitle("Image Transformation Job #" + id);
        this.setResizable(false);

        // Make this window non-closable
        this.setOnCloseRequest(Event::consume);

        // Create all sub-widgets in the window
        Label targetDirLabel = new Label("Target Directory:");
        targetDirLabel.setPrefWidth(115);

        // Create a "change target directory"  button
        this.changeDirButton = new Button("");
        this.changeDirButton.setId("changeDirButton");
        this.changeDirButton.setPrefHeight(buttonPreferredHeight);
        Image image = Util.loadImageFromResourceFile("folder-icon.png");
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(10);
        imageView.setFitHeight(10);
        this.changeDirButton.setGraphic(imageView);

        // Create a "target directory"  text field
        this.targetDirTextField = new TextField(this.targetDir.toString());
        this.targetDirTextField.setDisable(true);
        HBox.setHgrow(targetDirTextField, Priority.ALWAYS);

        // Create an informative label
        Label transformLabel = new Label("Transformation: ");
        transformLabel.setPrefWidth(115);

        //  Create the pull-down list of image transforms
        this.imgTransformList = new ComboBox<>();
        this.imgTransformList.setId("imgTransformList");  // For TestFX
        this.imgTransformList.setItems(FXCollections.observableArrayList(
                "Invert",
                "Solarize",
                "Oil4"
        ));

        this.imgTransformList.getSelectionModel().selectFirst();  //Chooses first imgTransform as default

        // Create a "Run" button
        this.runButton = new Button("Run job (on " + inputFiles.size() + " image" + (inputFiles.size() == 1 ? "" : "s") + ")");
        this.runButton.setId("runJobButton");
        this.runButton.setPrefHeight(buttonPreferredHeight);

        // Create the FileListWithViewPort display
        this.flwvp = new FileListWithViewPort(windowWidth * 0.98, windowHeight - 4 * buttonPreferredHeight - 3 * 5, false);
        this.flwvp.addFiles(inputFiles);

        // Create a "Close" button
        this.closeButton = new Button("Close");
        this.closeButton.setId("closeButton");
        this.closeButton.setPrefHeight(buttonPreferredHeight);

        // Create a "Cancel" button
        this.cancelButton = new Button("Cancel");
        this.cancelButton.setId("cancelButton");
        this.cancelButton.setPrefHeight(buttonPreferredHeight);
        this.cancelButton.setDisable(true);

        // Set actions for all widgets
        this.changeDirButton.setOnAction(e -> {
            DirectoryChooser dirChooser = new DirectoryChooser();
            dirChooser.setTitle("Choose target directory");
            File dir = dirChooser.showDialog(this);
            this.setTargetDir(Paths.get(dir.getAbsolutePath()));
        });

        this.runButton.setOnAction(e -> {
            this.closeButton.setDisable(true);
            this.changeDirButton.setDisable(true);
            this.runButton.setDisable(true);
            this.imgTransformList.setDisable(true);

            executeJob(imgTransformList.getSelectionModel().getSelectedItem());

            this.closeButton.setDisable(false);
        });

        this.closeButton.setOnAction(f -> this.close());

        // Build the scene
        VBox layout = new VBox(5);

        HBox row1 = new HBox(5);
        row1.setAlignment(Pos.CENTER_LEFT);
        row1.getChildren().add(targetDirLabel);
        row1.getChildren().add(changeDirButton);
        row1.getChildren().add(targetDirTextField);
        layout.getChildren().add(row1);

        HBox row2 = new HBox(5);
        row2.setAlignment(Pos.CENTER_LEFT);
        row2.getChildren().add(transformLabel);
        row2.getChildren().add(imgTransformList);
        layout.getChildren().add(row2);

        layout.getChildren().add(flwvp);

        HBox row3 = new HBox(5);
        row3.getChildren().add(runButton);
        row3.getChildren().add(closeButton);
        row3.getChildren().add(cancelButton);
        layout.getChildren().add(row3);

        // set the metrics label
        metricsLabel = new Label("");

        // Add the label to row 4
        HBox row4 = new HBox(5);
        row4.getChildren().add(metricsLabel);
        layout.getChildren().add(row4);

        Scene scene = new Scene(layout, windowWidth, windowHeight);

        // Pop up the new window
        this.setScene(scene);
        this.toFront();
        this.show();
    }

    /** 
     * Method to set the metricsLabel
     */
    public void setMetricsLabel(long totalTimens, long processingTimens, long writingTimens, long readingTimens) {
        Platform.runLater(() -> {
            this.metricsLabel.setText(
                "Total Execution Time: " + totalTimens / 1000000 + " ms" + 
                " Processing Time: " + processingTimens / 1000000 + " ms" +
                " Writing Time: " + writingTimens / 1000000 + " ms" + 
                " Reading Time: " + readingTimens / 1000000 + " ms");
        });
    }
    /**
     * Method to add a listener for the "window was closed" event
     *
     * @param listener The listener method
     */
    public void addCloseListener(Runnable listener) {
        this.addEventHandler(WindowEvent.WINDOW_HIDDEN, (event) -> listener.run());
    }

    /**
     * Method to set the target directory
     *
     * @param dir A directory
     */
    private void setTargetDir(Path dir) {
        if (dir != null) {
            this.targetDir = dir;
            this.targetDirTextField.setText(targetDir.toAbsolutePath().toString());
        }
    }

    /**
     * A method to execute the job
     *
     * @param filterName The name of the filter to apply to input images
     */
    private void executeJob(String filterName) {

        // Clear the display
        this.flwvp.clear();

        // Create a job
        Job job = new Job(filterName, this.targetDir, this.inputFiles, this);

        this.cancelButton.setDisable(false);

        this.cancelButton.setOnAction(e -> {
            job.cancel();
        });


        // create a thread that runs the job
        new Thread(() -> {

            // start the job timer
            long startTime = System.nanoTime();

            // disable the close button
            Platform.runLater(() -> {
                this.closeButton.setDisable(true);
            });
            
            StringBuilder errorMessage = new StringBuilder();

            Job.ImgTransformOutcome outcome; // The outcome of processing an image

            // Process the images
            while ((outcome = job.processNextImage()) != null) {
                Job.ImgTransformOutcome finalOutcome = outcome;

                // Update the viewport
                Platform.runLater(() -> {
                    if (finalOutcome.success) {
                        // Add the output file to the viewport
                        this.flwvp.addFile(finalOutcome.outputFile); 
                    } else {
                        // Append the error message to the error message string
                        errorMessage.append(finalOutcome.inputFile.toAbsolutePath()).append(": ").append(finalOutcome.error.getMessage()).append("\n");
                    }
                });
            }

            this.cancelButton.setDisable(true);

            // stop the job timer
            long endTime = System.nanoTime();
            long totalTime = endTime - startTime;

            Platform.runLater(() -> {

                // update the execution time labels
                if (job.isCancelled()) {
                    this.metricsLabel.setText("Job Cancelled");
                } else {
                    this.setMetricsLabel(totalTime, job.getTotalProcessingTime(), job.getTotalWritingTime(), job.getTotalReadingTime());
                }

                // Renable the close button
                this.closeButton.setDisable(false);

                // Display any errors after the job is complete
                if (!errorMessage.toString().isEmpty()) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("ImgTransform Job Error");
                    alert.setHeaderText(null);
                    alert.setContentText(errorMessage.toString());
                    alert.showAndWait();
                }
            });


        }).start();

    }
}
