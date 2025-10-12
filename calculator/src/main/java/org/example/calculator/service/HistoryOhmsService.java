package org.example.calculator.service;

import org.example.calculator.config.Database;
import org.example.calculator.model.OhmsRow;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HistoryOhmsService {

    /** admin=true -> все записи + логин; admin=false -> только по userId (логин не нужен) */
    public List<OhmsRow> loadOhms(boolean admin, Integer userId) {
        String sqlAdmin = """
            SELECT COALESCE(u.login,'(гость)') AS login,
                   h.i_value, h.i_unit, h.v_value, h.v_unit, h.r_value, h.r_unit
            FROM ohms_history h
            LEFT JOIN users u ON u.id = h.user_id
            ORDER BY h.created_at DESC
            """;

        String sqlUser = """
            SELECT NULL AS login, i_value, i_unit, v_value, v_unit, r_value, r_unit
            FROM ohms_history
            WHERE user_id = ?
            ORDER BY created_at DESC
            """;

        List<OhmsRow> out = new ArrayList<>();
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(admin ? sqlAdmin : sqlUser)) {

            if (!admin) ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(new OhmsRow(
                            rs.getString(1),
                            rs.getDouble(2), rs.getString(3),
                            rs.getDouble(4), rs.getString(5),
                            rs.getDouble(6), rs.getString(7)
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB error loading ohms history", e);
        }
        return out;
    }
}
