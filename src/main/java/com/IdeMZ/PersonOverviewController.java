package com.IdeMZ;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.fxmisc.richtext.StyleClassedTextArea;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class PersonOverviewController {

    private static final Logger logger = LogManager.getLogger(PersonOverviewController.class);

    @FXML
    private StyleClassedTextArea styleClassedTextArea;

    private SyntaxHighlighter syntaxHighlighter;
    private String selectedFilePath;

    //file button

    @FXML
    private void handleOpenFile() {
        FileChooser fileChooser = new FileChooser();
        // Use the window of styleClassedTextArea instead of fileButton
        Window window = styleClassedTextArea.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(window);
        if (selectedFile != null) {
            try {
                String content = Files.readString(selectedFile.toPath());
                styleClassedTextArea.replaceText(content);
                updateSyntaxHighlighting();

                selectedFilePath = selectedFile.getAbsolutePath();
            } catch (IOException e) {
                logger.error(e);
            }
        }
    }

    @FXML
    private void handleSaveButton() {
        if (selectedFilePath == null) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save File");
            File file = fileChooser.showSaveDialog(styleClassedTextArea.getScene().getWindow());
            if (file != null) {
                selectedFilePath = file.getAbsolutePath();
            } else {
                return;
            }
        }

        try {
            String content = styleClassedTextArea.getText();

            Files.writeString(Path.of(selectedFilePath), content);
        } catch (IOException e) {
            logger.error(e);
        }
    }

    @FXML
    private void handleExitButton() {
        Platform.exit();
    }

    //run button

    @FXML
    private void handleRunButton() {
        if (selectedFilePath == null) {
            // No file is selected, show a pop-up message
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Information");
            alert.setHeaderText(null);
            alert.setContentText("Please save the file before running.");

            alert.showAndWait();
        } else {
            try {
                ProcessBuilder processBuilder = new ProcessBuilder(
                        "java",
                        "-jar",
                        "CompilerMZ-0.6.0-Alpha-jar-with-dependencies.jar",
                        "-i",
                        selectedFilePath
                );

                Process process = processBuilder.start();

                // Read the output of the process and print it to the console
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }

                int exitCode = process.waitFor();
                System.out.println("Process exited with code " + exitCode);
            } catch (IOException | InterruptedException e) {
                logger.error(e);
            }
        }
    }

    @FXML
    public void initialize() {
        syntaxHighlighter = new SyntaxHighlighter();

        styleClassedTextArea.textProperty().addListener((obs, oldText, newText) -> updateSyntaxHighlighting());
    }

    // Method to update syntax highlighting
    private void updateSyntaxHighlighting() {
        Platform.runLater(() -> syntaxHighlighter.highlight(styleClassedTextArea));
    }
}
