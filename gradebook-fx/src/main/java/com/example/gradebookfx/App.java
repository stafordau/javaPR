package com.example.gradebookfx;

import com.example.gradebookfx.ui.ViewLoader;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) {
        var root = ViewLoader.loadMainView();
        var scene = new Scene(root, 980, 640);
        scene.getStylesheets().add(App.class.getResource("/css/grades-fx.css").toExternalForm());

        stage.setTitle("GradeBook — JavaFX");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
