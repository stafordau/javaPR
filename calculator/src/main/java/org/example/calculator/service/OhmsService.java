package org.example.calculator.service;

import org.example.calculator.config.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class OhmsService {

    public void saveOhmsRecord(Integer userId,
                               double v, String vUnit,
                               double i, String iUnit,
                               double r, String rUnit) {
        String sql = "INSERT INTO ohms_history(user_id, v_value, v_unit, i_value, i_unit, r_value, r_unit) " +
                "VALUES(?, ?, ?, ?, ?, ?, ?)";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            if (userId == null) ps.setNull(1, java.sql.Types.BIGINT);
            else ps.setInt(1, userId);
            ps.setDouble(2, v);  ps.setString(3, vUnit);
            ps.setDouble(4, i);  ps.setString(5, iUnit);
            ps.setDouble(6, r);  ps.setString(7, rUnit);
            ps.executeUpdate();
        } catch (Exception e) {}
    }
}
