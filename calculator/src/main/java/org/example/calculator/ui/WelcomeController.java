package org.example.calculator.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.example.calculator.model.User;
import org.example.calculator.session.Session;

public class WelcomeController {

    @FXML private Label welcomeLabel;

    @FXML
    private void initialize() {
        User u = Session.get();
        if (u != null) {
            if ("ADMIN".equalsIgnoreCase(u.getRole())) {
                welcomeLabel.setText("Администратор: " + u.getLogin());
            } else {
                welcomeLabel.setText("Добро пожаловать, " + u.getLogin());
            }
        } else {
            welcomeLabel.setText("Добро пожаловать, гость");
        }
    }

    @FXML
    private void onOhms() {
        openOrInfo("/ohms.fxml", "Закон Ома");
    }

    @FXML
    private void onDivider() {
        try {
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            Scene scene = new Scene(FXMLLoader.load(getClass().getResource("/divider.fxml")));
            scene.getStylesheets().add(getClass().getResource("/main.css").toExternalForm());
            stage.setScene(scene);
            stage.setMaximized(true); // окно во весь экран
            stage.show();

            stage.setTitle("Калькулятор делителя напряжения");
            stage.setScene(scene);
            stage.setMaximized(true);              // ← окно на весь экран
            stage.show();
        } catch (Exception ignored) {}
    }

    @FXML
    private void onLogout() {
        Session.logout();
        openOrInfo("/main.fxml", "Калькулятор — главный экран");
    }

    private void openOrInfo(String fxml, String title) {
        try {
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            Scene scene = new Scene(FXMLLoader.load(getClass().getResource(fxml)));
            stage.setTitle(title);
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();
        } catch (Exception e) {
            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setHeaderText(title);
            a.setContentText("Экран не реализован. Создай " + fxml + " и контроллер.");
            a.showAndWait();
        }
    }

    @FXML
    private void onHistory() {
        // простое всплывающее окно выбора раздела
        var ohms = new ButtonType("История Закон Ома", ButtonBar.ButtonData.OK_DONE);
        var divider = new ButtonType("История Делитель напряжения", ButtonBar.ButtonData.OTHER);
        var cancel = new ButtonType("Назад", ButtonBar.ButtonData.CANCEL_CLOSE);

        Alert a = new Alert(Alert.AlertType.NONE, "Что посмотреть?", ohms, divider, cancel);
        a.setHeaderText("Выберите раздел");
        var res = a.showAndWait().orElse(cancel);

        try {
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            if (res == ohms) {
                Scene scene = new Scene(FXMLLoader.load(getClass().getResource("/history_ohms.fxml")));
                scene.getStylesheets().add(getClass().getResource("/main.css").toExternalForm());
                stage.setScene(scene);
                stage.setMaximized(true); // окно во весь экран
                stage.show();

                stage.setTitle("История — Закон Ома");
                stage.setScene(scene);
                stage.setMaximized(true);                    // во весь экран
                stage.show();
            } else if (res == divider) {
                Scene scene = new Scene(FXMLLoader.load(getClass().getResource("/history_divider.fxml")));
                scene.getStylesheets().add(getClass().getResource("/main.css").toExternalForm());
                stage.setScene(scene);
                stage.setMaximized(true); // окно во весь экран
                stage.show();

                stage.setTitle("История — Делитель напряжения");
            }
        } catch (Exception ignored) {}
    }



}
