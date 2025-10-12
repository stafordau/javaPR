package org.example.calculator;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // ставим первую сцену
        Scene scene = new Scene(FXMLLoader.load(getClass().getResource("/main.fxml")));
        attachCss(scene);
        stage.setTitle("Калькулятор");
        stage.setScene(scene);
        stage.show();
        stage.setMaximized(true);
        stage.centerOnScreen();

        Rectangle2D a = Screen.getPrimary().getVisualBounds();
        stage.setX(a.getMinX());
        stage.setY(a.getMinY());
        stage.setWidth(a.getWidth());
        stage.setHeight(a.getHeight());

        stage.sceneProperty().addListener((obs, oldSc, newSc) -> {
            if (newSc != null) {
                attachCss(newSc);
                javafx.application.Platform.runLater(() -> {
                    stage.setMaximized(true);
                    stage.centerOnScreen();

                    if (!stage.isMaximized()) {
                        Rectangle2D b = Screen.getPrimary().getVisualBounds();
                        stage.setX(b.getMinX());
                        stage.setY(b.getMinY());
                        stage.setWidth(b.getWidth());
                        stage.setHeight(b.getHeight());
                    }
                });
            }
        });
    }

    private void attachCss(Scene sc) {
        var css = getClass().getResource("/main.css");
        if (css != null && !sc.getStylesheets().contains(css.toExternalForm())) {
            sc.getStylesheets().add(css.toExternalForm());
        }
    }
}
