package com.IdeMZ;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.MenuItem;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.fxmisc.richtext.StyleClassedTextArea;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class PersonOverviewController {

    @FXML
    private StyleClassedTextArea styleClassedTextArea;

    @FXML
    private MenuItem fileButton;

    private SyntaxHighlighter syntaxHighlighter;
    private String selectedFilePath;

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
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleRunButton() {
        if (selectedFilePath == null) {
            // No file is selected, show a pop-up message
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Information");
            alert.setHeaderText(null);
            alert.setContentText("Please select a file to run.");

            alert.showAndWait();
        } else {
            // A file is selected, run the process
            try {
                ProcessBuilder processBuilder = new ProcessBuilder(
                        "java",
                        "-jar",
                        "C:\\Users\\Utente\\IdeaProjects\\IdeMZ\\CompilerMZ-0.6.0-Alpha-jar-with-dependencies.jar",
                        "-i",
                        selectedFilePath
                );

                Process process = processBuilder.start();
                int exitCode = process.waitFor();
                System.out.println("Process exited with code " + exitCode);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleSaveButton() {
        if (selectedFilePath != null) {
            try {
                String content = styleClassedTextArea.getText();

                Files.writeString(Path.of(selectedFilePath), content);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void initialize() {
        // Create a SyntaxHighlighter instance
        syntaxHighlighter = new SyntaxHighlighter();

        // Add a listener to the text property of the StyleClassedTextArea
        styleClassedTextArea.textProperty().addListener((obs, oldText, newText) -> {
            // Call updateSyntaxHighlighting method when text changes
            updateSyntaxHighlighting();
        });
    }

    // Method to update syntax highlighting
    private void updateSyntaxHighlighting() {
        Platform.runLater(() -> {
            syntaxHighlighter.highlight(styleClassedTextArea);
        });
    }
}
