package com.IdeMZ;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.fxmisc.richtext.StyleClassedTextArea;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class IdeMZApplication extends Application {
    private static final Logger LOGGER = Logger.getLogger(IdeMZApplication.class.getName());
    private final StyleClassedTextArea textArea = new StyleClassedTextArea();

    @Override
    public void start(Stage primaryStage) {
        FileOpener fileOpener = new FileOpener(primaryStage);

        // Create a SyntaxHighlighter instance
        SyntaxHighlighter syntaxHighlighter = new SyntaxHighlighter();

        // Add this line after you've initialized your textArea
        textArea.textProperty().addListener((obs, oldText, newText) -> syntaxHighlighter.highlight(textArea));

        // Create the "Open File" button
        Button openFileButton = new Button();
        // Create the "Open File" button
        MenuButton settingsButton = new MenuButton();

        // Create ImageView instances for the gear images
        ImageView settingsBlackIcon = new ImageView(new Image(Objects.requireNonNull(getClass().getResource("/images/gear_black.png")).toExternalForm()));
        ImageView settingsWhiteIcon = new ImageView(new Image(Objects.requireNonNull(getClass().getResource("/images/gear_white.png")).toExternalForm()));

        // Set the fit width and height of the ImageView instances
        settingsBlackIcon.setFitWidth(20);
        settingsBlackIcon.setFitHeight(20);
        settingsWhiteIcon.setFitWidth(20);
        settingsWhiteIcon.setFitHeight(20);

        // Set the initial image for the "Settings" button
        settingsButton.setGraphic(settingsBlackIcon);
        settingsButton.setText("");

        // Create ImageView instances for the images
        ImageView openFileBlackIcon = new ImageView(new Image(Objects.requireNonNull(getClass().getResource("/images/file_black.png")).toExternalForm()));
        ImageView openFileWhiteIcon = new ImageView(new Image(Objects.requireNonNull(getClass().getResource("/images/file_white.png")).toExternalForm()));

        // Set the fit width and height of the ImageView instances
        openFileBlackIcon.setFitWidth(20);
        openFileBlackIcon.setFitHeight(20);
        openFileWhiteIcon.setFitWidth(20);
        openFileWhiteIcon.setFitHeight(20);

        // Set the initial image for the "Open File" button
        openFileButton.setGraphic(openFileBlackIcon);
        openFileButton.setText("");

        openFileButton.setOnAction(event -> {
            File file = fileOpener.openFile();
            if (file != null) {
                try {
                    String content = Files.readString(file.toPath());
                    textArea.replaceText(content);
                    syntaxHighlighter.highlight(textArea);
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "An IO exception occurred", e);
                }
            }
        });

        MenuItem styleButton = new MenuItem("Style");
        settingsButton.getItems().add(styleButton);

        // Create the style dialog
        Dialog<Void> styleDialog = new Dialog<>();
        styleDialog.initModality(Modality.NONE); // Allow input events to other windows
        styleDialog.setTitle("Style");
        Button darkModeButton = new Button("Dark Mode");
        Button lightModeButton = new Button("Light Mode");
        VBox dialogVBox = new VBox(darkModeButton, lightModeButton);
        styleDialog.getDialogPane().setContent(dialogVBox);

        // Set the minimum width and height of the dialog pane
        styleDialog.getDialogPane().setMinWidth(500);
        styleDialog.getDialogPane().setMinHeight(300);

        HBox hbox = new HBox();

        // Set the initial styles for dark mode
        textArea.setStyle("-fx-background-color: #222831; -fx-text-fill: #EEEEEE;");
        textArea.getStyleClass().add("dark"); // Add the dark style class to the textArea
        hbox.setStyle("-fx-background-color: #31363F;");
        openFileButton.setGraphic(openFileWhiteIcon); // Set the white icon for the "Open File" button
        settingsButton.setGraphic(settingsWhiteIcon); // Set the white icon for the "Settings" button
        openFileButton.setStyle("-fx-background-color: #31363F;"); // Set the button color to match the hbox color in dark mode
        settingsButton.setStyle("-fx-background-color: #31363F;"); // Set the button color to match the hbox color in dark mode


        // Change the image, button color, and non-highlighted text color when the "Dark Mode" button is clicked
        darkModeButton.setOnAction(event -> {
            textArea.setStyle("-fx-background-color: #222831; -fx-text-fill: #FFFFFF;"); // Set the text color to white in dark mode
            textArea.getStyleClass().add("dark"); // Add the dark style class to the textArea
            hbox.setStyle("-fx-background-color: #31363F;");
            openFileButton.setGraphic(openFileWhiteIcon); // Set the white icon for the "Open File" button
            settingsButton.setGraphic(settingsWhiteIcon); // Set the white icon for the "Settings" button
            openFileButton.setStyle("-fx-background-color: #31363F;"); // Set the button color to match the hbox color in dark mode
            settingsButton.setStyle("-fx-background-color: #31363F;"); // Set the button color to match the hbox color in dark mode
            styleDialog.close();
        });

        // Change the image, button color, and non-highlighted text color when the "Light Mode" button is clicked
        lightModeButton.setOnAction(event -> {
            for (int i = 0; i < textArea.getLength(); i++) {
                textArea.setStyleClass(i, i+1, "default"); // Reset the style class for each character
            }
            textArea.setStyle("-fx-background-color: #FFFFFF; -fx-text-fill: #000000;"); // Set the text color to black in light mode
            textArea.getStyleClass().remove("dark"); // Remove the dark style class from the textArea
            hbox.setStyle("-fx-background-color: #FFFFFF;");
            openFileButton.setGraphic(openFileBlackIcon); // Set the black icon for the "Open File" button
            settingsButton.setGraphic(settingsBlackIcon); // Set the black icon for the "Settings" button
            openFileButton.setStyle("-fx-background-color: #FFFFFF;"); // Set the button color to match the hbox color in light mode
            settingsButton.setStyle("-fx-background-color: #FFFFFF;"); // Set the button color to match the hbox color in light mode
            styleDialog.close();
        });

        // Close the dialog when a mouse event is received outside the dialog
        styleDialog.getDialogPane().getScene().addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
            if (!styleDialog.getDialogPane().getBoundsInParent().contains(event.getX(), event.getY())) {
                styleDialog.close();
            }
        });

        // Close the dialog when the "X" button is clicked
        styleDialog.setOnCloseRequest(event -> {
            styleDialog.setResult(null);
            styleDialog.close();
            event.consume(); // Consume the event to prevent the window system from closing the window
        });

        // Show the style dialog when the "Style" button is clicked
        styleButton.setOnAction(event -> styleDialog.show());

        hbox.getChildren().addAll(openFileButton, settingsButton);
        hbox.setFillHeight(true); // This line ensures that the HBox takes up all available height

        VBox vbox = new VBox(hbox, textArea); // Add hbox and textArea to the VBox
        vbox.setFillWidth(true); // This line ensures that the VBox takes up all available width

        // Set VBox's Vgrow property for textArea to always
        VBox.setVgrow(textArea, Priority.ALWAYS);

        Scene scene = new Scene(vbox, 800, 600);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles.css")).toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}