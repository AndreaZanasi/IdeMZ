package com.IdeMZ;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.fxmisc.richtext.StyleClassedTextArea;

import java.io.File;
import java.io.IOException;
import java.nio.charset.MalformedInputException;
import java.nio.file.*;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public class IdeMZApplication extends Application {
    public static final String COMPILER_JAR_PATH = "src/main/resources/CompilerMZ-1.0.1-Fix.jar";
    private static final Logger LOGGER = Logger.getLogger(IdeMZApplication.class.getName());
    private final StyleClassedTextArea textArea = new StyleClassedTextArea();
    private boolean isDarkMode = false;
    private HBox hbox;
    BorderPane leftPane = new BorderPane();
    SplitPane splitPane = new SplitPane();
    Label footerLabel = new Label();
    HBox footer = new HBox(footerLabel);
    private Button openFileButton;
    private MenuButton settingsButton;
    private MenuButton translateButton;
    private Button openDirectoryButton;
    private Button saveFileButton;
    private Button runButton;
    private Button infoButton;
    private FileOpener fileOpener;
    private File currentFile;
    private File selectedDirectory;
    private final TreeView<File> directoryTreeView = new TreeView<>();
    private WatchService watchService;
    private String currentDialect = "default_dialect";
    SyntaxHighlighter syntaxHighlighter = new SyntaxHighlighter("default_dialect");


    @Override
    public void start(Stage primaryStage) {

        primaryStage.setTitle("IdeMZ");

        Image applicationIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/logo.png")));
        primaryStage.getIcons().add(applicationIcon);
        fileOpener = new FileOpener(primaryStage);

        textArea.getStyleClass().add("text-area-big-font");
        textArea.textProperty().addListener((obs, oldText, newText) -> syntaxHighlighter.highlight(textArea, isDarkMode));

        //buttons
        openFileButton = createButton();
        configureOpenFileButton();

        saveFileButton = createButton();
        configureSaveFileButton(primaryStage);

        runButton = createButton();
        configureRunButton();

        translateButton = createMenuButton();
        configureTranslateButton();

        settingsButton = createMenuButton();
        configureSettingsButton();

        openDirectoryButton = createButton();
        configureOpenDirectoryButton(primaryStage);

        infoButton = createButton();
        configureInfoButton();

        //hbox for buttons
        hbox = new HBox(openFileButton, openDirectoryButton, saveFileButton, runButton, translateButton, settingsButton, infoButton);
        hbox.setSpacing(20);
        hbox.setFillHeight(true);

        //left pane for directory tree view
        leftPane.setCenter(directoryTreeView);

        setDarkModeStyle();

        //vbox for hbox and text area
        VBox vbox = new VBox(hbox, textArea);
        vbox.setFillWidth(true);
        VBox.setVgrow(textArea, Priority.ALWAYS);

        textArea.getStyleClass().add("text-area-big-font");
        textArea.caretPositionProperty().addListener((obs, oldPosition, newPosition) -> {
            int line = textArea.getCurrentParagraph();
            int col = textArea.getCaretColumn();
            footerLabel.setText("Line: " + (line + 1) + ", Column: " + (col + 1));
        });

        //hbox for footer
        footer.setPadding(new Insets(5, 10, 5, 10)); // Optional padding

        //split pane for left pane and text area
        splitPane.getItems().addAll(leftPane, textArea);
        splitPane.setDividerPositions(0.2);

        //main pane
        BorderPane mainLayout = new BorderPane();
        mainLayout.setTop(hbox);
        mainLayout.setLeft(leftPane);
        mainLayout.setCenter(splitPane);
        mainLayout.setBottom(footer);

        Scene scene = new Scene(mainLayout, 800, 600);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles.css")).toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void configureOpenFileButton() {
        openFileButton.setPrefSize(20, 20);
        openFileButton.setOnAction(event -> {
            File file = fileOpener.openFile();
            if (file != null) {
                currentFile = file; // Save the opened file to the currentFile variable
                Path filePath = file.toPath();
                try {
                    String content = Files.readString(filePath);
                    textArea.replaceText(content);
                    syntaxHighlighter.highlight(textArea, isDarkMode);
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "An IO exception occurred in configureOpenButton", e);
                }
            }
        });
    }

    private void configureSaveFileButton(Stage primaryStage) {
        AtomicBoolean newlyCreated = new AtomicBoolean(false);
        saveFileButton.setPrefSize(20, 20);
        saveFileButton.setOnAction(event -> {
            if (currentFile == null || !currentFile.exists()) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Save File");
                currentFile = fileChooser.showSaveDialog(primaryStage);
                if (currentFile == null) {
                    return;
                }
                newlyCreated.set(true);
            }
            try {
                if(newlyCreated.get()) {
                    Files.writeString(currentFile.toPath(), textArea.getText(), StandardOpenOption.CREATE);
                    newlyCreated.set(false);
                }else{
                    Files.writeString(currentFile.toPath(), textArea.getText(), StandardOpenOption.TRUNCATE_EXISTING);
                }

                String command = String.format("java -jar %s -i %s --format", COMPILER_JAR_PATH, currentFile.getAbsolutePath());
                executeCommand(command);
                String content = Files.readString(currentFile.toPath());
                textArea.replaceText(content);
                syntaxHighlighter.highlight(textArea, isDarkMode);
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "An IO exception occurred in configureSaveButton", e);
            }
        });
    }

    private void configureRunButton() {
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
                    String command = getString();
                    Runtime.getRuntime().exec(new String[]{"gnome-terminal", "--", "bash", "-c", command});
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "An IO exception occurred in configureRunButton", e);
                }
            }
        });
    }

    private String getString() {
        String filePath = currentFile.getAbsolutePath();
        String filePathWithoutExtension = filePath.substring(0, filePath.lastIndexOf('.'));
        String command;
        if (!currentDialect.equals("default_dialect")) {
            command = String.format("java -jar %s -i %s -d %s && %s; exec bash", COMPILER_JAR_PATH, filePath, currentDialect, filePathWithoutExtension);
        } else {
            command = String.format("java -jar %s -i %s && %s; exec bash", COMPILER_JAR_PATH, filePath, filePathWithoutExtension);
        }
        return command;
    }

    private void configureTranslateButton() {
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
                        String command = String.format("java -jar %s -i %s -t %s,%s", COMPILER_JAR_PATH, currentFilePath, currentDialect, dialectName);
                        executeCommand(command);

                        // Reload the text area with the updated file
                        String content = Files.readString(currentFile.toPath());
                        textArea.replaceText(content);

                        // Update the SyntaxHighlighter with the new dialect
                        syntaxHighlighter.updateDialect(dialectName);
                        syntaxHighlighter.highlight(textArea, isDarkMode);

                        // Update the current dialect
                        currentDialect = dialectName;
                    } catch (IOException e) {
                        LOGGER.log(Level.SEVERE, "An exception occurred", e);
                    }
                });
                translateButton.getItems().add(menuItem);
            }
        }
    }

    private void configureSettingsButton() {
        settingsButton.setPrefSize(20, 20);
        Dialog<Void> styleDialog = createStyleDialog();
        MenuItem styleButton = new MenuItem("Style");
        settingsButton.getItems().add(styleButton);
        styleButton.setOnAction(event -> styleDialog.show());
    }

    private void configureOpenDirectoryButton(Stage primaryStage) {
        directoryTreeView.setCellFactory(tv -> new TreeCell<>() {
            @Override
            protected void updateItem(File item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? "" : item.getName());
            }
        });

        directoryTreeView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue.getValue().isFile()) {
                currentFile = newValue.getValue();
                try {
                    String content = Files.readString(currentFile.toPath());
                    textArea.replaceText(content);
                    syntaxHighlighter.highlight(textArea, isDarkMode);
                }
                catch (MalformedInputException e) {
                    LOGGER.log(Level.WARNING, "Unreadable file", e);
                }
                catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "An IO exception occurred in configureOpenDirectoryButton", e);
                }

            }
        });

        openDirectoryButton.setPrefSize(20, 20);
        openDirectoryButton.setOnAction(event -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            selectedDirectory = directoryChooser.showDialog(primaryStage);
            if (selectedDirectory != null) {
                updateDirectoryTreeView(selectedDirectory);
                watchDirectory(selectedDirectory.toPath());
            }
        });


    }

    private void updateDirectoryTreeView(File file) {
        TreeItem<File> rootItem = createNode(file);
        directoryTreeView.setRoot(rootItem);
        expandTreeView(rootItem);

    }

    private void expandTreeView(TreeItem<File> item) {
        if (item != null && !item.isLeaf()) {
            item.setExpanded(true);
            for (TreeItem<File> child : item.getChildren()) {
                expandTreeView(child);
            }
        }
    }

    private void watchDirectory(Path path) {
        try {
            if (watchService != null) {
                watchService.close();
            }
            watchService = FileSystems.getDefault().newWatchService();
            path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);

            Thread watchThread = new Thread(() -> {
                try {
                    while (true) {
                        WatchKey key = watchService.take();
                        for (WatchEvent<?> ignore : key.pollEvents()) {
                            javafx.application.Platform.runLater(() -> updateDirectoryTreeView(selectedDirectory));
                        }
                        boolean valid = key.reset();
                        if (!valid) {
                            break;
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (ClosedWatchServiceException e) {
                    // Watch service closed, do nothing
                }
            });
            watchThread.setDaemon(true);
            watchThread.start();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "An IO exception occurred in watchDirectory", e);
        }
    }


    private void configureInfoButton() {
        infoButton.setPrefSize(20, 20);
        infoButton.setOnAction(event -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("IdeMZ Information");
            alert.setHeaderText("IdeMZ is an Integrated Development Environment for the MZ language");
            alert.setContentText("It provides syntax highlighting, file management, and the ability to run and translate MZ programs.\nManaged by Davide Mecugni and Andrea Zanasi.");
            // Get the version from COMPILER_JAR_PATH string
            String compilerVersion = COMPILER_JAR_PATH.substring(COMPILER_JAR_PATH.lastIndexOf('-') + 1, COMPILER_JAR_PATH.lastIndexOf('.'));
            alert.setContentText(alert.getContentText() + "\n\nCompilerMZ version: " + compilerVersion);
            alert.showAndWait();
        });
    }

    private TreeItem<File> createNode(final File file) {
        return new TreeItem<>(file) {
            private boolean isFirstTimeChildren = true;
            private boolean isFirstTimeLeaf = true;
            private boolean isLeaf;

            @Override
            public String toString() {
                return this.getValue().getName();
            }

            @Override
            public ObservableList<TreeItem<File>> getChildren() {
                if (isFirstTimeChildren) {
                    isFirstTimeChildren = false;
                    super.getChildren().setAll(buildChildren(this));
                }
                return super.getChildren();
            }

            @Override
            public boolean isLeaf() {
                if (isFirstTimeLeaf) {
                    isFirstTimeLeaf = false;
                    isLeaf = this.getValue().isFile();
                }
                return isLeaf;
            }

            private ObservableList<TreeItem<File>> buildChildren(TreeItem<File> treeItem) {
                File file = treeItem.getValue();
                if (file != null && file.isDirectory()) {
                    File[] files = file.listFiles();
                    if (files != null) {
                        ObservableList<TreeItem<File>> children = FXCollections.observableArrayList();
                        for (File childFile : files) {
                            TreeItem<File> childItem = createNode(childFile);
                            children.add(childItem);
                            childItem.addEventHandler(TreeItem.branchExpandedEvent(), event -> {
                                if (childItem.isLeaf() && childItem.getValue().isFile()) {
                                    currentFile = childItem.getValue();
                                    try {
                                        String content = Files.readString(currentFile.toPath());
                                        textArea.replaceText(content);
                                        syntaxHighlighter.highlight(textArea, isDarkMode);
                                    } catch (IOException e) {
                                        LOGGER.log(Level.SEVERE, "An IO exception occurred in createNode for dir view", e);
                                    }
                                }
                            });
                        }
                        return children;
                    }
                }
                return FXCollections.emptyObservableList();
            }
        };
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

        HBox buttonBox = getBox(dialogVBox);

        dialogVBox.getChildren().add(buttonBox);

        styleDialog.getDialogPane().setContent(dialogVBox);
        styleDialog.getDialogPane().setMinWidth(400);
        styleDialog.getDialogPane().setMinHeight(200);

        styleDialog.getDialogPane().getScene().getWindow().setOnCloseRequest(event -> styleDialog.close());

        return styleDialog;
    }

    private HBox getBox(VBox dialogVBox) {
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
        footer.setStyle(darkModeColor);
        footerLabel.setStyle("-fx-text-fill: #FFFFFF;");

        setButtonStyleAndGraphic(openFileButton, darkModeColor, new Image(Objects.requireNonNull(getClass().getResource("/images/file_white.png")).toExternalForm()));
        setButtonStyleAndGraphic(settingsButton, darkModeColor, new Image(Objects.requireNonNull(getClass().getResource("/images/gear_white.png")).toExternalForm()));
        setButtonStyleAndGraphic(saveFileButton, darkModeColor, new Image(Objects.requireNonNull(getClass().getResource("/images/save_white.png")).toExternalForm()));
        setButtonStyleAndGraphic(runButton, darkModeColor, new Image(Objects.requireNonNull(getClass().getResource("/images/play_white.png")).toExternalForm()));
        setButtonStyleAndGraphic(translateButton, darkModeColor, new Image(Objects.requireNonNull(getClass().getResource("/images/world_white.png")).toExternalForm()));
        setButtonStyleAndGraphic(openDirectoryButton, darkModeColor, new Image(Objects.requireNonNull(getClass().getResource("/images/folder_white.png")).toExternalForm()));
        setButtonStyleAndGraphic(infoButton, darkModeColor, new Image(Objects.requireNonNull(getClass().getResource("/images/info_white.png")).toExternalForm()));

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
        footer.setStyle(lightModeColor);
        footerLabel.setStyle("-fx-text-fill: #000000;");

        setButtonStyleAndGraphic(openFileButton, lightModeColor, new Image(Objects.requireNonNull(getClass().getResource("/images/file_black.png")).toExternalForm()));
        setButtonStyleAndGraphic(settingsButton, lightModeColor, new Image(Objects.requireNonNull(getClass().getResource("/images/gear_black.png")).toExternalForm()));
        setButtonStyleAndGraphic(saveFileButton, lightModeColor, new Image(Objects.requireNonNull(getClass().getResource("/images/save_black.png")).toExternalForm()));
        setButtonStyleAndGraphic(runButton, lightModeColor, new Image(Objects.requireNonNull(getClass().getResource("/images/play_black.png")).toExternalForm()));
        setButtonStyleAndGraphic(translateButton, lightModeColor, new Image(Objects.requireNonNull(getClass().getResource("/images/world_black.png")).toExternalForm()));
        setButtonStyleAndGraphic(openDirectoryButton, lightModeColor, new Image(Objects.requireNonNull(getClass().getResource("/images/folder_black.png")).toExternalForm()));
        setButtonStyleAndGraphic(infoButton, lightModeColor, new Image(Objects.requireNonNull(getClass().getResource("/images/info_black.png")).toExternalForm()));

        textArea.getStyleClass().remove("dark");
        textArea.setStyle("-fx-fill: black;");

        isDarkMode = false;
    }

    private void setButtonStyleAndGraphic(ButtonBase button, String style, Image image) {
        button.setStyle(style);
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(30.0);
        imageView.setFitHeight(30.0);
        button.setGraphic(imageView);
    }

    private void executeCommand(String command) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c", command);
            Process process = processBuilder.start();
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            LOGGER.log(Level.SEVERE, "An exception occurred", e);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}