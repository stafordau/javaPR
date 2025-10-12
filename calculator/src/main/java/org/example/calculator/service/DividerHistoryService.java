package org.example.calculator.service;

import org.example.calculator.config.Database;
import org.example.calculator.model.DividerResult;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class DividerHistoryService {

    private static final String INSERT_SQL = """
        INSERT INTO divider_history(
          user_id,
          vin, v_required, tol_pct, series, rmin, rmax,
          top_type, top_r1, top_r2, top_eff,
          bottom_type, bottom_r1, bottom_r2, bottom_eff,
          vout, err_pct, elements, power_mw
        )
        VALUES(?, ?, ?, ?, ?, ?, ?,
               ?, ?, ?, ?,
               ?, ?, ?, ?,
               ?, ?, ?, ?)
        """;

    public void save(Integer userId,
                     double vin, double vRequired, double tolPct,
                     String series, double rmin, double rmax,
                     DividerResult r) {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(INSERT_SQL)) {

            if (userId == null) ps.setNull(1, java.sql.Types.BIGINT);
            else ps.setInt(1, userId);

            ps.setDouble(2, vin);
            ps.setDouble(3, vRequired);
            ps.setDouble(4, tolPct);
            ps.setString(5, series);
            ps.setDouble(6, rmin);
            ps.setDouble(7, rmax);

            ps.setString(8,  r.getTop().type());
            setPart(ps, 9,  r.getTop().parts(), 0);
            setPart(ps, 10, r.getTop().parts(), 1);
            ps.setDouble(11, r.getTop().effective());

            ps.setString(12, r.getBottom().type());
            setPart(ps, 13, r.getBottom().parts(), 0);
            setPart(ps, 14, r.getBottom().parts(), 1);
            ps.setDouble(15, r.getBottom().effective());

            ps.setDouble(16, r.getVout());
            ps.setDouble(17, r.getErrorPct());
            ps.setInt(18,    r.getCount());
            ps.setDouble(19, r.getPowerMilli());

            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Ошибка сохранения divider_history", e);
        }
    }

    private void setPart(PreparedStatement ps, int idx, java.util.List<Double> parts, int i) throws Exception {
        if (parts != null && parts.size() > i) ps.setDouble(idx, parts.get(i));
        else ps.setNull(idx, java.sql.Types.DOUBLE);
    }
}
