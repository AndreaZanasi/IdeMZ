package com.IdeMZ;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

public class IdeMZApplication extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/person-overview-view.fxml")));
        Scene scene = new Scene(root);
        stage.setTitle("IdeMZ");
        stage.setScene(scene);
        stage.show();
    }
}