package org.example.calculator.ui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import org.example.calculator.session.Session;

import java.io.IOException;

public class MainController {

    @FXML
    private void onLogin(ActionEvent e) {
        if (Session.isAuth()) {
            openSceneOrNotify("/welcome.fxml", "Добро пожаловать", getStage(e));
        } else {
            openSceneOrNotify("/login.fxml", "Вход", getStage(e));
        }
    }

    @FXML
    private void onRegister(ActionEvent e) {
        Stage stage = getStage(e);
        openSceneOrNotify("/register.fxml", "Регистрация", stage);
    }

    @FXML
    private void onExit(ActionEvent e) {
        javafx.application.Platform.exit();
    }


    private Stage getStage(ActionEvent e) {
        return (Stage) ((Node) e.getSource()).getScene().getWindow();
    }

    private void openSceneOrNotify(String fxmlPath, String title, Stage stage) {
        try {
            var url = getClass().getResource(fxmlPath);
            if (url == null) throw new IOException("FXML not found: " + fxmlPath);

            FXMLLoader loader = new FXMLLoader(url);
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(getClass().getResource("/main.css").toExternalForm());
            stage.setTitle(title);
            stage.setScene(scene);
            stage.setMaximized(true);                    // во весь экран
            stage.show();
        } catch (IOException ex) {
            ex.printStackTrace(); // ← в консоли будет точная строка/причина
            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setHeaderText(title);
            a.setContentText("Не удалось загрузить " + fxmlPath + ":\n" + ex.getMessage());
            a.showAndWait();
        }
    }

}
