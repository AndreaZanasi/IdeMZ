package com.IdeMZ;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.fxmisc.richtext.StyleClassedTextArea;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
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
    private MenuButton translateButton;
    private File currentFile;
    private Button saveFileButton;
    private Button runButton;
    private String currentDialect = "default_dialect";


    @Override
    public void start(Stage primaryStage) {

        Image applicationIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/logo.png")));
        primaryStage.getIcons().add(applicationIcon);

        FileOpener fileOpener = new FileOpener(primaryStage);
        SyntaxHighlighter syntaxHighlighter = new SyntaxHighlighter("default_dialect");

        textArea.textProperty().addListener((obs, oldText, newText) -> syntaxHighlighter.highlight(textArea));

        openFileButton = createButton();
        openFileButton.setOnAction(event -> {
            File file = fileOpener.openFile();
            if (file != null) {
                currentFile = file; // Save the opened file to the currentFile variable
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

        saveFileButton = new Button();
        saveFileButton.setPrefSize(20, 20);
        saveFileButton.setOnAction(event -> {
            if (currentFile == null || !currentFile.exists()) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Save File");
                currentFile = fileChooser.showSaveDialog(primaryStage);
                if (currentFile == null) {
                    // User didn't choose a file, return
                    return;
                }
            }
            try {
                Files.writeString(currentFile.toPath(), textArea.getText(), StandardOpenOption.CREATE);
                String command = String.format("java -jar src/main/resources/CompilerMZ-1.0.0-Stable-jar-with-dependencies.jar -i %s --format", currentFile.getAbsolutePath());
                ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c", command);
                Process process = processBuilder.start();
                int exitCode = process.waitFor();
                if (exitCode != 0) {
                    LOGGER.log(Level.SEVERE, "The process exited with error code: " + exitCode);
                    return;
                }
                // Reload the text area with the updated file
                String content = Files.readString(currentFile.toPath());
                textArea.replaceText(content);
                syntaxHighlighter.highlight(textArea);
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "An IO exception occurred", e);
            } catch (InterruptedException e) {
                LOGGER.log(Level.SEVERE, "An InterruptedException occurred", e);
            }
        });

        runButton = new Button();
        runButton.setPrefSize(20, 20);
        runButton.setOnAction(event -> {
            if (currentFile == null || !currentFile.exists()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Warning Dialog");
                alert.setHeaderText(null);
                alert.setContentText("Before running the file please save it.");
                alert.showAndWait();
            } else {
                try {
                    String filePath = currentFile.getAbsolutePath();
                    String filePathWithoutExtension = filePath.substring(0, filePath.lastIndexOf('.'));
                    String command = String.format("java -jar src/main/resources/CompilerMZ-1.0.0-Stable-jar-with-dependencies.jar -i %s && %s; exec bash", filePath, filePathWithoutExtension);
                    Runtime.getRuntime().exec(new String[]{"gnome-terminal", "--", "bash", "-c", command});
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "An IO exception occurred", e);
                }
            }
        });

        translateButton = new MenuButton("Translate");
        translateButton.setPrefSize(20, 20);
        File dialectsDir = new File(Objects.requireNonNull(getClass().getResource("/dialects")).getFile());
        File[] dialectFiles = dialectsDir.listFiles((dir, name) -> name.endsWith(".json"));
        if (dialectFiles != null) {
            for (File dialectFile : dialectFiles) {
                String dialectName = dialectFile.getName().replace(".json", "");
                MenuItem menuItem = new MenuItem(dialectName);
                menuItem.setOnAction(event -> {
                    try {
                        if (currentFile == null || !currentFile.exists()) {
                            Alert alert = new Alert(Alert.AlertType.WARNING);
                            alert.setTitle("Warning Dialog");
                            alert.setHeaderText(null);
                            alert.setContentText("Before translate the file, please save the file.");
                            alert.showAndWait();
                            return;
                        }
                        String currentFilePath = currentFile.getAbsolutePath();
                        String command = String.format("java -jar src/main/resources/CompilerMZ-1.0.0-Stable-jar-with-dependencies.jar -i %s -t %s,%s", currentFilePath, currentDialect, dialectName);
                        ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c", command);
                        Process process = processBuilder.start();
                        int exitCode = process.waitFor();
                        if (exitCode != 0) {
                            LOGGER.log(Level.SEVERE, "The process exited with error code: " + exitCode);
                            return;
                        }

                        // Reload the text area with the updated file
                        String content = Files.readString(currentFile.toPath());
                        textArea.replaceText(content);

                        // Update the SyntaxHighlighter with the new dialect
                        syntaxHighlighter.updateDialect(dialectName);
                        syntaxHighlighter.highlight(textArea);

                        // Update the current dialect
                        currentDialect = dialectName;
                    } catch (IOException | InterruptedException e) {
                        LOGGER.log(Level.SEVERE, "An exception occurred", e);
                    }
                });
                translateButton.getItems().add(menuItem);
            }
        }

        settingsButton = createMenuButton();
        Dialog<Void> styleDialog = createStyleDialog();
        MenuItem styleButton = new MenuItem("Style");
        settingsButton.getItems().add(styleButton);
        styleButton.setOnAction(event -> styleDialog.show());

        hbox = new HBox(openFileButton, settingsButton, saveFileButton, runButton, translateButton);
        hbox.setSpacing(20);
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
        saveFileButton.setStyle(darkModeColor);
        runButton.setStyle(darkModeColor);
        translateButton.setStyle(darkModeColor);

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

        // Inside the setDarkModeStyle method, after setting the graphic for the openFileButton
        Image save_white = new Image(Objects.requireNonNull(getClass().getResource("/images/save_white.png")).toExternalForm());
        ImageView save_white_view = new ImageView(save_white);
        save_white_view.setFitWidth(30.0);
        save_white_view.setFitHeight(30.0);

        // Inside the setDarkModeStyle method, after setting the graphic for the saveFileButton
        Image play_white = new Image(Objects.requireNonNull(getClass().getResource("/images/play_white.png")).toExternalForm());
        ImageView play_white_view = new ImageView(play_white);
        play_white_view.setFitWidth(30.0);
        play_white_view.setFitHeight(30.0);

        Image world_white = new Image(Objects.requireNonNull(getClass().getResource("/images/world_white.png")).toExternalForm());
        ImageView world_white_view = new ImageView(world_white);
        world_white_view.setFitWidth(30.0);
        world_white_view.setFitHeight(30.0);

        openFileButton.setGraphic(file_white_view);
        settingsButton.setGraphic(gear_white_view);
        saveFileButton.setGraphic(save_white_view);
        runButton.setGraphic(play_white_view);
        translateButton.setGraphic(world_white_view);

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
        saveFileButton.setStyle(lightModeColor);
        runButton.setStyle(lightModeColor);
        translateButton.setStyle(lightModeColor);

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

        // Inside the setLightModeStyle method, after setting the graphic for the openFileButton
        Image save_black = new Image(Objects.requireNonNull(getClass().getResource("/images/save_black.png")).toExternalForm());
        ImageView save_black_view = new ImageView(save_black);
        save_black_view.setFitWidth(30.0);
        save_black_view.setFitHeight(30.0);

        // Inside the setLightModeStyle method, after setting the graphic for the saveFileButton
        Image play_black = new Image(Objects.requireNonNull(getClass().getResource("/images/play_black.png")).toExternalForm());
        ImageView play_black_view = new ImageView(play_black);
        play_black_view.setFitWidth(30.0);
        play_black_view.setFitHeight(30.0);

        Image world_black = new Image(Objects.requireNonNull(getClass().getResource("/images/world_black.png")).toExternalForm());
        ImageView world_black_view = new ImageView(world_black);
        world_black_view.setFitWidth(30.0);
        world_black_view.setFitHeight(30.0);

        openFileButton.setGraphic(file_black_view);
        settingsButton.setGraphic(gear_black_view);
        saveFileButton.setGraphic(save_black_view);
        runButton.setGraphic(play_black_view);
        translateButton.setGraphic(world_black_view);

        textArea.getStyleClass().remove("dark");
        textArea.setStyle("-fx-fill: black;");

        isDarkMode = false;
    }

    public static void main(String[] args) {
        launch(args);
    }
}