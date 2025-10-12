package org.example.calculator.config;

import java.sql.*;

public class Database {
    public static Connection getConnection() throws SQLException {
        String url = Config.DB_URL + Config.DB_NAME + "?useSSL=false&serverTimezone=UTC&characterEncoding=utf8";
        return DriverManager.getConnection(url, Config.DB_USER, Config.DB_PASSWORD);
    }
}
