package com.example.gradebookfx.ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.io.IOException;

public final class ViewLoader {
    private ViewLoader() {}

    public static Parent loadMainView() {
        try {
            FXMLLoader loader = new FXMLLoader(ViewLoader.class.getResource("/fxml/main.fxml"));
            return loader.load();
        } catch (IOException e) {
            throw new IllegalStateException("Cannot load main.fxml", e);
        }
    }
}
