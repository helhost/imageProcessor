
package ics432.imgapp;

import javafx.application.Platform;
import javafx.scene.control.Alert;

public class JobExecutor {

    private Job job;
    private JobWindow jobWindow;
    private long imagesProcessed = 0;

    public JobExecutor(Job job, JobWindow jobWindow) {
        this.job = job;
        this.jobWindow = jobWindow;
    }

    public void executeJob() {
        new Thread(() -> {

            // make the progress bar visible
            this.jobWindow.showProgress(true);

            // start the job timer
            long startTime = System.nanoTime();

            // disable the close button
            Platform.runLater(() -> {
                this.jobWindow.disableCloseButton(false);
            });

            StringBuilder errorMessage = new StringBuilder();

            Job.ImgTransformOutcome outcome; // The outcome of processing an image

            // Process the images
            while ((outcome = job.processNextImage()) != null) {
                Job.ImgTransformOutcome finalOutcome = outcome;

                this.imagesProcessed++;
                ; // Increment the number of images processed

                // Update the viewport
                Platform.runLater(() -> {
                    if (finalOutcome.success) {
                        // Add the output file to the viewport
                        this.jobWindow.addFile(finalOutcome.outputFile);
                    } else {
                        // Append the error message to the error message string
                        errorMessage.append(finalOutcome.inputFile.toAbsolutePath()).append(": ")
                                .append(finalOutcome.error.getMessage()).append("\n");
                    }

                    // Update the metrics label to show the number of images processed
                    this.jobWindow.updateProgress((double) this.imagesProcessed / this.jobWindow.getAmountOfImages());
                });
            }

            this.jobWindow.disableCloseButton(true);

            // hide and reset the progress bar
            this.jobWindow.showProgress(false);
            this.jobWindow.updateProgress(0);

            // stop the job timer
            long endTime = System.nanoTime();
            long totalTime = endTime - startTime;

            Platform.runLater(() -> {

                // update the execution time labels
                if (job.isCancelled()) {
                    this.jobWindow.updateMetricsLabel("Job Cancelled");
                } else {
                    this.jobWindow.setMetricsLabel(totalTime, job.getTotalProcessingTime(), job.getTotalWritingTime(),
                            job.getTotalReadingTime());
                }

                // Re-enable the close button
                this.jobWindow.disableCloseButton(false);

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