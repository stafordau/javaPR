package org.example.calculator.ui;

import javafx.scene.control.Alert;
import org.example.calculator.model.User;
import org.example.calculator.session.Session;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.calculator.service.AuthService;

public class RegisterController {

    @FXML private TextField loginField;
    @FXML private PasswordField passField;
    @FXML private PasswordField confirmField;
    @FXML private Label errorLabel;

    // этот сервис внутри уже хэширует BCrypt и пишет в MySQL
    private final AuthService auth = new AuthService();

    @FXML
    private void onRegister(ActionEvent e) {
        errorLabel.setText("");

        String login = loginField.getText() == null ? "" : loginField.getText().trim().toLowerCase();
        String pass  = passField.getText() == null ? "" : passField.getText();
        String rep   = confirmField.getText() == null ? "" : confirmField.getText();

        // валидация (как у тебя было)
        if (login.isEmpty() || pass.isEmpty() || rep.isEmpty()) { errorLabel.setText("Заполните все поля"); return; }
        if (login.length() < 3) { errorLabel.setText("Логин минимум 3 символа"); return; }
        if (pass.length() < 6) { errorLabel.setText("Пароль минимум 6 символов"); return; }
        if (!pass.equals(rep)) { errorLabel.setText("Пароли не совпадают"); return; }

        try {
            auth.register(login, pass);          // регистрация в БД (BCrypt внутри)
            User u = auth.login(login, pass);    // сразу логинимся
            if (u != null) {
                Session.login(u);                // кладём в сессию
                goToWelcome(e);                  // и на экран приветствия
            } else {
                errorLabel.setText("Не удалось войти после регистрации");
            }
        } catch (IllegalStateException ex) {
            errorLabel.setText(ex.getMessage()); // "Логин уже занят"
        } catch (Exception ex) {
            errorLabel.setText("Ошибка регистрации");
        }
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

    @FXML
    private void onCancel(ActionEvent e) {
        goToMain(e);
    }

    private void goToMain(ActionEvent e) {
        try {
            Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
            Scene scene = new Scene(FXMLLoader.load(getClass().getResource("/main.fxml")));
            scene.getStylesheets().add(getClass().getResource("/main.css").toExternalForm());

            stage.setTitle("Калькулятор — главный экран");
            stage.setScene(scene);
            stage.setMaximized(true);                    // во весь экран
            stage.show();
        } catch (Exception ex) {
            errorLabel.setText("Не удалось открыть главный экран");
        }
    }
}
