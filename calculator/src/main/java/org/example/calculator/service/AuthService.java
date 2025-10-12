package org.example.calculator.service;

import org.example.calculator.config.Database;
import org.example.calculator.model.User;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;

public class AuthService {

    public void register(String login, String rawPassword) {
        String normalized = login.trim().toLowerCase();
        String hash = BCrypt.hashpw(rawPassword, BCrypt.gensalt(12));

        String sql = "INSERT INTO users(login, pass_hash, role) VALUES(?, ?, ?)";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, normalized);
            ps.setString(2, hash);
            ps.setString(3, "USER"); // по умолчанию
            ps.executeUpdate();
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                throw new IllegalStateException("Логин уже занят");
            }
            throw new RuntimeException("DB error on register", e);
        }
    }

    public User login(String login, String rawPassword) {
        String sql = "SELECT id, login, pass_hash, role FROM users WHERE login = ?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, login.trim().toLowerCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                int id = rs.getInt("id");
                String dbLogin = rs.getString("login");
                String hash = rs.getString("pass_hash");
                String role = rs.getString("role");

                if (BCrypt.checkpw(rawPassword, hash)) {
                    return new User(id, dbLogin, hash, role);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB error on login", e);
        }
    }
}
