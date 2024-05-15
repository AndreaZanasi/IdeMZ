package com.IdeMZ;

import javafx.application.Platform;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.Node;
import javafx.event.ActionEvent;
import org.fxmisc.richtext.StyleClassedTextArea;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class PersonOverviewController {
    @FXML
    private Button fileButton;
    @FXML
    private TextArea textArea;

    private StyleClassedTextArea styleClassedTextArea;
    private SyntaxHighlighter syntaxHighlighter;

    @FXML
    private void handleOpenFile(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        Window window = ((Node) event.getTarget()).getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(window);
        if (selectedFile != null) {
            try {
                String content = Files.readString(selectedFile.toPath());
                styleClassedTextArea.replaceText(content);
                syntaxHighlighter.highlight(styleClassedTextArea);
                styleClassedTextArea.textProperty().addListener((obs, oldText, newText) -> {
                    // Call the highlight method every time the text changes
                    Platform.runLater(() -> {
                        syntaxHighlighter.highlight(styleClassedTextArea);
                    });
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void initialize() {
        // Instantiate styleClassedTextArea
        styleClassedTextArea = new StyleClassedTextArea();

        // Create a SyntaxHighlighter instance
        syntaxHighlighter = new SyntaxHighlighter();

        // Get the parent of the textArea
        AnchorPane parent = (AnchorPane) textArea.getParent();

        // Remove the textArea from its parent
        parent.getChildren().remove(textArea);

        // Add the styleClassedTextArea to the parent
        parent.getChildren().add(styleClassedTextArea);

        // Set the same layout constraints for the styleClassedTextArea as the textArea
        AnchorPane.setTopAnchor(styleClassedTextArea, AnchorPane.getTopAnchor(textArea));
        AnchorPane.setBottomAnchor(styleClassedTextArea, AnchorPane.getBottomAnchor(textArea));
        AnchorPane.setLeftAnchor(styleClassedTextArea, AnchorPane.getLeftAnchor(textArea));
        AnchorPane.setRightAnchor(styleClassedTextArea, AnchorPane.getRightAnchor(textArea));

        // Add a listener to the text property of the StyleClassedTextArea
        styleClassedTextArea.textProperty().addListener((obs, oldText, newText) -> {
            Platform.runLater(() -> {
                syntaxHighlighter.highlight(styleClassedTextArea);
            });
        });
    }
}