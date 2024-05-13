package com.IdeMZ;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.fxmisc.richtext.StyleClassedTextArea;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class IdeMZApplication extends Application {
    private static final Logger LOGGER = Logger.getLogger(IdeMZApplication.class.getName());
    private final StyleClassedTextArea textArea = new StyleClassedTextArea();
    private boolean isDarkMode = false;
    private HBox hbox; // Add this line
    private Button openFileButton; // Add this line
    private MenuButton settingsButton;

    @Override
    public void start(Stage primaryStage) {
        FileOpener fileOpener = new FileOpener(primaryStage);
        SyntaxHighlighter syntaxHighlighter = new SyntaxHighlighter();

        textArea.textProperty().addListener((obs, oldText, newText) -> syntaxHighlighter.highlight(textArea));

        openFileButton = createButton(); // Modify this line
        settingsButton = createMenuButton(); // Modify this line

        openFileButton.setOnAction(event -> {
            File file = fileOpener.openFile();
            if (file != null) {
                Path filePath = file.toPath();
                try {
                    String content = Files.readString(filePath);
                    textArea.replaceText(content);
                    syntaxHighlighter.highlight(textArea);
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "An IO exception occurred", e);
                }
            }
        });

        Dialog<Void> styleDialog = createStyleDialog();
        MenuItem styleButton = new MenuItem("Style");
        settingsButton.getItems().add(styleButton);
        styleButton.setOnAction(event -> styleDialog.show());

        hbox = new HBox(openFileButton, settingsButton);
        hbox.setFillHeight(true);

        setDarkModeStyle(); // Move this line here

        VBox vbox = new VBox(hbox, textArea);
        vbox.setFillWidth(true);
        VBox.setVgrow(textArea, Priority.ALWAYS);

        Scene scene = new Scene(vbox, 800, 600);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles.css")).toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private Button createButton() {
        Button button = new Button();
        ImageView blackIcon = new ImageView(new Image(Objects.requireNonNull(getClass().getResource("/images/file_black.png")).toExternalForm()));
        ImageView whiteIcon = new ImageView(new Image(Objects.requireNonNull(getClass().getResource("/images/file_white.png")).toExternalForm()));
        blackIcon.setFitWidth(15);
        blackIcon.setFitHeight(15);
        blackIcon.setPreserveRatio(true);
        whiteIcon.setFitWidth(15);
        whiteIcon.setFitHeight(15);
        whiteIcon.setPreserveRatio(true);
        button.setGraphic(blackIcon);
        button.setText("");
        button.setPrefSize(20, 20); // Set preferred size to 20x20
        return button;
    }

    private MenuButton createMenuButton() {
        MenuButton button = new MenuButton();
        ImageView blackIcon = new ImageView(new Image(Objects.requireNonNull(getClass().getResource("/images/gear_black.png")).toExternalForm()));
        ImageView whiteIcon = new ImageView(new Image(Objects.requireNonNull(getClass().getResource("/images/gear_white.png")).toExternalForm()));
        blackIcon.setFitWidth(15);
        blackIcon.setFitHeight(15);
        blackIcon.setPreserveRatio(true);
        whiteIcon.setFitWidth(15);
        whiteIcon.setFitHeight(15);
        whiteIcon.setPreserveRatio(true);
        button.setGraphic(blackIcon);
        button.setText("");
        button.setPrefSize(20, 20); // Set preferred size to 20x20
        return button;
    }

    private Dialog<Void> createStyleDialog() {
        Dialog<Void> styleDialog = new Dialog<>();
        styleDialog.initModality(Modality.NONE);
        styleDialog.setTitle("Style");

        Button darkModeButton = new Button("Dark Mode");
        darkModeButton.setOnAction(event -> setDarkModeStyle());
        Button lightModeButton = new Button("Light Mode");
        lightModeButton.setOnAction(event -> setLightModeStyle());

        VBox dialogVBox = new VBox(darkModeButton, lightModeButton);
        styleDialog.getDialogPane().setContent(dialogVBox);
        styleDialog.getDialogPane().setMinWidth(500);
        styleDialog.getDialogPane().setMinHeight(300);

        return styleDialog;
    }

    private void setDarkModeStyle() {
        if (isDarkMode) {
            return;
        }

        String darkModeColor = "-fx-background-color: #31363F;";

        textArea.setStyle("-fx-background-color: #222831; -fx-text-fill: #EEEEEE;");
        hbox.setStyle(darkModeColor);
        openFileButton.setStyle(darkModeColor);
        settingsButton.setStyle(darkModeColor);
        openFileButton.setGraphic(new ImageView(new Image(Objects.requireNonNull(getClass().getResource("/images/file_white.png")).toExternalForm())));
        settingsButton.setGraphic(new ImageView(new Image(Objects.requireNonNull(getClass().getResource("/images/gear_white.png")).toExternalForm())));
        textArea.getStyleClass().add("dark");
        isDarkMode = true;
    }

    private void setLightModeStyle() {
        if (!isDarkMode) {
            return;
        }

        String lightModeColor = "-fx-background-color: #9394A5;";

        textArea.setStyle("-fx-background-color: #fafafa; -fx-text-fill: #000000;");
        hbox.setStyle(lightModeColor);
        openFileButton.setStyle(lightModeColor);
        settingsButton.setStyle(lightModeColor);
        openFileButton.setGraphic(new ImageView(new Image(Objects.requireNonNull(getClass().getResource("/images/file_black.png")).toExternalForm())));
        settingsButton.setGraphic(new ImageView(new Image(Objects.requireNonNull(getClass().getResource("/images/gear_black.png")).toExternalForm())));
        textArea.getStyleClass().remove("dark");
        textArea.setStyle("-fx-fill: black;"); // Directly apply the style to the text

        isDarkMode = false;
    }
    public static void main(String[] args) {
        launch(args);
    }
}