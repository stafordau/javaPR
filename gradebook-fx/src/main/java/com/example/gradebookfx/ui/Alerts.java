package com.example.gradebookfx.ui;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;

public final class Alerts {
    private Alerts() {}

    public static void error(String title, String content) {
        Alert a = new Alert(Alert.AlertType.ERROR, content, ButtonType.OK);
        a.setHeaderText(title);
        a.showAndWait();
    }

    public static boolean confirm(String title, String content) {
        ButtonType ok = new ButtonType("ОК", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancel = new ButtonType("Отменить", ButtonBar.ButtonData.CANCEL_CLOSE);

        Alert a = new Alert(Alert.AlertType.CONFIRMATION, content, ok, cancel);
        a.setHeaderText(title);

        return a.showAndWait().orElse(cancel) == ok;
    }


    public static void info(String title, String content) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, content, ButtonType.OK);
        a.setHeaderText(title);
        a.showAndWait();
    }
}
