package com.IdeMZ;

import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class FileOpener {
    private final Stage stage;

    public FileOpener(Stage stage) {
        this.stage = stage;
    }

    public File openFile() {
        FileChooser fileChooser = new FileChooser();
        return fileChooser.showOpenDialog(stage);
    }
}