package com.IdeMZ;

import javafx.application.Application;
import javafx.geometry.Pos;
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
    private HBox hbox;
    private Button openFileButton;
    private MenuButton settingsButton;

    @Override
    public void start(Stage primaryStage) {
        FileOpener fileOpener = new FileOpener(primaryStage);
        SyntaxHighlighter syntaxHighlighter = new SyntaxHighlighter();

        textArea.textProperty().addListener((obs, oldText, newText) -> syntaxHighlighter.highlight(textArea));

        openFileButton = createButton();
        settingsButton = createMenuButton();

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

        setDarkModeStyle();

        VBox vbox = new VBox(hbox, textArea);
        vbox.setFillWidth(true);
        VBox.setVgrow(textArea, Priority.ALWAYS);

        openFileButton.setPrefSize(20, 20);
        settingsButton.setPrefSize(20, 20);

        textArea.getStyleClass().add("text-area-big-font");

        Scene scene = new Scene(vbox, 800, 600);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles.css")).toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private Button createButton() {
        return new Button();
    }

    private MenuButton createMenuButton() {
        return new MenuButton();
    }

    private Dialog<Void> createStyleDialog() {
        Dialog<Void> styleDialog = new Dialog<>();
        styleDialog.initModality(Modality.NONE);
        styleDialog.setTitle("Style");

        VBox dialogVBox = new VBox();
        dialogVBox.setAlignment(Pos.CENTER);

        HBox buttonBox = gethBox(dialogVBox);

        dialogVBox.getChildren().add(buttonBox);

        if (isDarkMode) {
            dialogVBox.setStyle("-fx-background-color: #31363F;");
        } else {
            dialogVBox.setStyle("-fx-background-color: #9394A5;");
        }

        styleDialog.getDialogPane().setContent(dialogVBox);
        styleDialog.getDialogPane().setMinWidth(500);
        styleDialog.getDialogPane().setMinHeight(300);

        styleDialog.getDialogPane().getScene().getWindow().setOnCloseRequest(event -> styleDialog.close());

        return styleDialog;
    }

    private HBox gethBox(VBox dialogVBox) {
        Button darkModeButton = new Button("Dark Mode");
        darkModeButton.setOnAction(event -> {
            setDarkModeStyle();
            dialogVBox.setStyle("-fx-background-color: #31363F;");
        });
        darkModeButton.setPrefSize(100, 100);

        Button lightModeButton = new Button("Light Mode");
        lightModeButton.setOnAction(event -> {
            setLightModeStyle();
            dialogVBox.setStyle("-fx-background-color: #9394A5;");
        });
        lightModeButton.setPrefSize(100, 100);

        HBox buttonBox = new HBox(darkModeButton, lightModeButton);
        buttonBox.setSpacing(10);
        buttonBox.setAlignment(Pos.CENTER);
        return buttonBox;
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

        double imageWidth = 30.0;
        double imageHeight = 30.0;

        Image file_white = new Image(Objects.requireNonNull(getClass().getResource("/images/file_white.png")).toExternalForm());
        ImageView file_white_view = new ImageView(file_white);
        file_white_view.setFitWidth(imageWidth);
        file_white_view.setFitHeight(imageHeight);

        Image gear_white = new Image(Objects.requireNonNull(getClass().getResource("/images/gear_white.png")).toExternalForm());
        ImageView gear_white_view = new ImageView(gear_white);
        gear_white_view.setFitWidth(imageWidth);
        gear_white_view.setFitHeight(imageHeight);

        openFileButton.setGraphic(file_white_view);
        settingsButton.setGraphic(gear_white_view);

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

        double imageWidth = 30.0;
        double imageHeight = 30.0;

        Image file_black = new Image(Objects.requireNonNull(getClass().getResource("/images/file_black.png")).toExternalForm());
        ImageView file_black_view = new ImageView(file_black);
        file_black_view.setFitWidth(imageWidth);
        file_black_view.setFitHeight(imageHeight);

        Image gear_black = new Image(Objects.requireNonNull(getClass().getResource("/images/gear_black.png")).toExternalForm());
        ImageView gear_black_view = new ImageView(gear_black);
        gear_black_view.setFitWidth(imageWidth);
        gear_black_view.setFitHeight(imageHeight);

        openFileButton.setGraphic(file_black_view);
        settingsButton.setGraphic(gear_black_view);

        textArea.getStyleClass().remove("dark");
        textArea.setStyle("-fx-fill: black;");

        isDarkMode = false;
    }

    public static void main(String[] args) {
        launch(args);
    }
}