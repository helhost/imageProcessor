package ics432.imgapp;

import com.jhlabs.image.InvertFilter;
import com.jhlabs.image.OilFilter;
import com.jhlabs.image.SolarizeFilter;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javafx.application.Platform;

import static javax.imageio.ImageIO.createImageOutputStream;

/**
 * A class that defines the "job" abstraction, that is, a  set of input image files
 * to which a filter must be applied, thus generating a set of output
 * image files. Each output file name is the input file name prepended with
 * the ImgTransform name and an underscore.
 */
class Job {

    // reference to the job window
    private JobWindow jobWindow;

    private final String filterName;
    private final Path targetDir;
    private final List<Path> inputFiles;

    private long totalProcessingTime = 0;
    private long totalReadingTime = 0;
    private long totalWritingTime = 0;

    public long getTotalProcessingTime() {
        return totalProcessingTime;
    }

    public long getTotalReadingTime() {
        return totalReadingTime;
    }

    public long getTotalWritingTime() {
        return totalWritingTime;
    }

    /**
     * Constructor
     *
     * @param filterName The name of the filter to apply to input images
     * @param targetDir  The target directory in which to generate output images
     * @param inputFiles The list of input file paths
     */
    Job(String filterName,
        Path targetDir,
        List<Path> inputFiles,
        JobWindow jobWindow) {

        this.filterName = filterName;
        this.targetDir = targetDir;
        this.inputFiles = inputFiles;

        this.jobWindow = jobWindow;
    }

    /** 
     * udated method to process the next image
    */
    public ImgTransformOutcome processNextImage() {
        if (inputFiles.isEmpty()) {
            return null;
        }

        Path inputFile = inputFiles.remove(0);
        Path outputFile;
        ImgTransformOutcome outcome;

        try {
            outputFile = processInputFile(inputFile);
            // Generate a "success" outcome
            outcome = new ImgTransformOutcome(true, inputFile, outputFile, null);
        } catch (IOException e) {
            // Generate a "failure" outcome
            outcome = new ImgTransformOutcome(false, inputFile, null, e);
        }

        return outcome;
    }

    /**
     * Helper method to apply a imgTransform to an input image file
     *
     * @param inputFile The input file path
     * @return the output file path
     */
    private Path processInputFile(Path inputFile) throws IOException {

        // Load the image from file
        long startReadingTime = System.nanoTime();

        Image image;
        try {
            image = new Image(inputFile.toUri().toURL().toString());
            if (image.isError()) {
                throw new IOException("Error while reading from " + inputFile.toAbsolutePath() +
                        " (" + image.getException().toString() + ")");
            }
        } catch (IOException e) {
            throw new IOException("Error while reading from " + inputFile.toAbsolutePath());
        }

        long endReadingTime = System.nanoTime();
        this.totalReadingTime += endReadingTime - startReadingTime;

        // Create the filter
        BufferedImageOp filter = createFilter(filterName);

        // Process the image
        long startProcessingTime = System.nanoTime();

        BufferedImage img = filter.filter(SwingFXUtils.fromFXImage(image, null), null);

        long endProcessingTime = System.nanoTime();
        this.totalProcessingTime += endProcessingTime - startProcessingTime;

        // Write the image back to a file
        long startWritingTime = System.nanoTime();

        String outputPath = this.targetDir + FileSystems.getDefault().getSeparator() + this.filterName + "_" + inputFile.getFileName();
        try {
            OutputStream os = new FileOutputStream(outputPath);
            ImageOutputStream outputStream = createImageOutputStream(os);
            ImageIO.write(img, "jpg", outputStream);
        } catch (IOException | NullPointerException e) {
            throw new IOException("Error while writing to " + outputPath);
        }

        long endWritingTime = System.nanoTime();
        this.totalWritingTime += endWritingTime - startWritingTime;

        // Success!
        return Paths.get(outputPath);
    }

    /**
     * A helper method to create a Filter object
     *
     * @param filterName the filter's name
     */
    private BufferedImageOp createFilter(String filterName) {
        switch (filterName) {
            case "Invert":
                return new InvertFilter();
            case "Solarize":
                return new SolarizeFilter();
            case "Oil4":
                OilFilter oil4Filter = new OilFilter();
                oil4Filter.setRange(4);
                return oil4Filter;
            default:
                throw new RuntimeException("Unknown filter " + filterName);
        }
    }

    /**
     * A helper nested class to define a imgTransform outcome for a given input file and ImgTransform
     */
    static class ImgTransformOutcome {

        // Whether the image transform is successful or not
        final boolean success;
        // The Input File path
        final Path inputFile;
        // The output file path (or null if failure)
        final Path outputFile;
        // The exception that was raised (or null if success)
        final Exception error;

        /**
         * Constructor
         *
         * @param success     Whether the imgTransform operation worked
         * @param input_file  The input file path
         * @param output_file The output file path  (null if success is false)
         * @param error       The exception raised (null if success is true)
         */
        ImgTransformOutcome(boolean success, Path input_file, Path output_file, Exception error) {
            this.success = success;
            this.inputFile = input_file;
            this.outputFile = output_file;
            this.error = error;
        }

    }
}
