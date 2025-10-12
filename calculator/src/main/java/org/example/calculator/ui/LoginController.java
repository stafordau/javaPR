package org.example.calculator.ui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.calculator.model.User;
import org.example.calculator.service.AuthService;
import org.example.calculator.session.Session;

public class LoginController {

    @FXML private TextField loginField;
    @FXML private PasswordField passField;
    @FXML private Label errorLabel;

    private final AuthService auth = new AuthService();

    @FXML
    private void onLogin(ActionEvent e) {
        errorLabel.setText("");
        String login = loginField.getText() == null ? "" : loginField.getText().trim().toLowerCase();
        String pass  = passField.getText() == null ? "" : passField.getText();

        if (login.isEmpty() || pass.isEmpty()) {
            errorLabel.setText("Введите логин и пароль");
            return;
        }

        try {
            User u = auth.login(login, pass);
            if (u == null) {
                errorLabel.setText("Неверный логин или пароль");
                return;
            }
            Session.login(u);
            goToWelcome(e);
        } catch (Exception ex) {
            errorLabel.setText("Ошибка входа");
        }
    }

    @FXML
    private void onCancel(ActionEvent e) {
        goToMain(e);
    }

    private void goToWelcome(ActionEvent e) {
        try {
            Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
            Scene scene = new Scene(FXMLLoader.load(getClass().getResource("/welcome.fxml")));
            scene.getStylesheets().add(getClass().getResource("/main.css").toExternalForm());

            stage.setTitle("Добро пожаловать");
            stage.setScene(scene);
            stage.setMaximized(true);                    // во весь экран
            stage.show();
        } catch (Exception ex) {
            errorLabel.setText("Не удалось открыть экран приветствия");
        }
    }

    private void goToMain(ActionEvent e) {
        try {
            Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
            Scene scene = new Scene(FXMLLoader.load(getClass().getResource("/main.fxml")));
            stage.setTitle("Калькулятор — главный экран");
            stage.setScene(scene);
            stage.setMaximized(true);                    // во весь экран
            stage.show();
        } catch (Exception ex) {
            errorLabel.setText("Не удалось открыть главный экран");
        }
    }
}
