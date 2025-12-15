package com.example.gradebookfx.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class AppConfig {
    private static final Properties PROPS = new Properties();

    static {
        try (InputStream is = AppConfig.class.getResourceAsStream("/app.properties")) {
            if (is != null) {
                PROPS.load(is);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read app.properties", e);
        }
    }

    private AppConfig() {}

    public static String apiBaseUrl() {
        return PROPS.getProperty("api.baseUrl", "http://localhost:8080");
    }
}
