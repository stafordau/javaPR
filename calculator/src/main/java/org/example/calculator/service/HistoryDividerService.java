package org.example.calculator.service;

import org.example.calculator.config.Database;
import org.example.calculator.model.DividerHistoryRow;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** Читает таблицу divider_history для экрана истории делителя. */
public class HistoryDividerService {

    public List<DividerHistoryRow> load(boolean isAdmin, Integer userId) {
        String SQL_ADMIN = """
            SELECT COALESCE(u.login,'(гость)') AS login,
                   CONCAT(
                        CASE h.top_type WHEN '1' THEN 'Верх: 1' WHEN 'S' THEN 'Верх: S2' ELSE 'Верх: P2' END,
                        ' | ',
                        CASE h.bottom_type WHEN '1' THEN 'Низ: 1' WHEN 'S' THEN 'Низ: S2' ELSE 'Низ: P2' END,
                        ' (', (CASE h.top_type WHEN '1' THEN 1 ELSE 2 END) + (CASE h.bottom_type WHEN '1' THEN 1 ELSE 2 END), ')'
                   ) AS topology,
                   CONCAT(
                        'Верх: ',
                        IF(h.top_type='S', CONCAT(FORMAT(h.top_r1,3), ' + ', FORMAT(h.top_r2,3)),
                           IF(h.top_type='P', CONCAT(FORMAT(h.top_r1,3), ' || ', FORMAT(h.top_r2,3)),
                                               FORMAT(h.top_r1,3))),
                        ' → ', FORMAT(h.top_eff,3), ' Ω',
                        ' | Низ: ',
                        IF(h.bottom_type='S', CONCAT(FORMAT(h.bottom_r1,3), ' + ', FORMAT(h.bottom_r2,3)),
                           IF(h.bottom_type='P', CONCAT(FORMAT(h.bottom_r1,3), ' || ', FORMAT(h.bottom_r2,3)),
                                                  FORMAT(h.bottom_r1,3))),
                        ' → ', FORMAT(h.bottom_eff,3), ' Ω'
                   ) AS values_txt,
                   h.vout, h.err_pct, h.elements, h.power_mw,
                   DATE_FORMAT(h.created_at, '%Y-%m-%d %H:%i') AS created_at
            FROM divider_history h
            LEFT JOIN users u ON u.id = h.user_id
            ORDER BY h.created_at DESC
        """;

        String SQL_USER = """
            SELECT NULL AS login,
                   CONCAT(
                        CASE h.top_type WHEN '1' THEN 'Верх: 1' WHEN 'S' THEN 'Верх: S2' ELSE 'Верх: P2' END,
                        ' | ',
                        CASE h.bottom_type WHEN '1' THEN 'Низ: 1' WHEN 'S' THEN 'Низ: S2' ELSE 'Низ: P2' END,
                        ' (', (CASE h.top_type WHEN '1' THEN 1 ELSE 2 END) + (CASE h.bottom_type WHEN '1' THEN 1 ELSE 2 END), ')'
                   ) AS topology,
                   CONCAT(
                        'Верх: ',
                        IF(h.top_type='S', CONCAT(FORMAT(h.top_r1,3), ' + ', FORMAT(h.top_r2,3)),
                           IF(h.top_type='P', CONCAT(FORMAT(h.top_r1,3), ' || ', FORMAT(h.top_r2,3)),
                                               FORMAT(h.top_r1,3))),
                        ' → ', FORMAT(h.top_eff,3), ' Ω',
                        ' | Низ: ',
                        IF(h.bottom_type='S', CONCAT(FORMAT(h.bottom_r1,3), ' + ', FORMAT(h.bottom_r2,3)),
                           IF(h.bottom_type='P', CONCAT(FORMAT(h.bottom_r1,3), ' || ', FORMAT(h.bottom_r2,3)),
                                                  FORMAT(h.bottom_r1,3))),
                        ' → ', FORMAT(h.bottom_eff,3), ' Ω'
                   ) AS values_txt,
                   h.vout, h.err_pct, h.elements, h.power_mw,
                   DATE_FORMAT(h.created_at, '%Y-%m-%d %H:%i') AS created_at
            FROM divider_history h
            WHERE h.user_id = ?
            ORDER BY h.created_at DESC
        """;

        List<DividerHistoryRow> list = new ArrayList<>();
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(isAdmin ? SQL_ADMIN : SQL_USER)) {

            if (!isAdmin) ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new DividerHistoryRow(
                            rs.getString(1),   // login
                            rs.getString(2),   // topology
                            rs.getString(3),   // values
                            rs.getDouble(4),   // vout
                            rs.getDouble(5),   // err
                            rs.getInt(6),      // elements
                            rs.getDouble(7),   // power
                            rs.getString(8)    // created
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Не удалось загрузить историю делителя", e);
        }
        return list;
    }
}
