package org.example.calculator.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.example.calculator.model.DividerResult;
import org.example.calculator.service.DividerHistoryService;
import org.example.calculator.service.DividerService;
import org.example.calculator.session.Session;

import java.util.Locale;

public class DividerController {

    @FXML private TextField vinField, vreqField, tolField, rminField, rmaxField;
    @FXML private ComboBox<String> seriesBox;

    @FXML private Label topologyLabel, valuesLabel, voutLabel, errLabel, countLabel, powerLabel, statusLabel;

    @FXML private Canvas canvas;
    @FXML private StackPane canvasPane;

    private final DividerService svc = new DividerService();
    private final DividerHistoryService history = new DividerHistoryService();

    @FXML
    private void initialize() {
        seriesBox.getItems().addAll("E6", "E12", "E24", "E96");
        seriesBox.getSelectionModel().select("E24");

        canvas.widthProperty().bind(canvasPane.widthProperty());
        canvas.heightProperty().bind(canvasPane.heightProperty());
        canvas.widthProperty().addListener((o,a,b)-> redraw(null));
        canvas.heightProperty().addListener((o,a,b)-> redraw(null));

        clearOutput();
    }

    @FXML
    private void onCalculate() {
        try {
            double vin  = parse(vinField.getText());
            double vreq = parse(vreqField.getText());
            double tol  = parse(tolField.getText());
            double rmin = parse(rminField.getText());
            double rmax = parse(rmaxField.getText());
            DividerService.Series series = DividerService.Series.valueOf(seriesBox.getValue());

            long t0 = System.currentTimeMillis();
            DividerResult best = svc.searchBest(vin, vreq, tol, series, rmin, rmax);
            long ms = System.currentTimeMillis() - t0;

            if (best == null) {
                clearOutput();
                statusLabel.setText("Подходящих схем не найдено (" + ms + " мс)");
                return;
            }

            redraw(best);

            topologyLabel.setText(best.getTopology().replace("Верх:", "Схема: Верх"));
            valuesLabel.setText(best.prettyValues().replace("Top:", "Верх:").replace("Bottom:", "Низ:"));
            voutLabel.setText(fmt(best.getVout()));
            errLabel.setText(fmt(best.getErrorPct()));
            countLabel.setText(Integer.toString(best.getCount()));
            powerLabel.setText(fmt(best.getPowerMilli()));
            statusLabel.setText("Готово: подобран лучший вариант за " + ms + " мс (автосохранено)");

            var u = Session.get();
            Integer userId = (u == null) ? null : u.getId();
            history.save(userId, vin, vreq, tol, series.name(), rmin, rmax, best);

        } catch (Exception e) {
            clearOutput();
            statusLabel.setText("Ошибка входных данных");
        }
    }

    @FXML
    private void onBack() {
        try {
            Stage stage = (Stage) canvas.getScene().getWindow();
            Scene scene = new Scene(FXMLLoader.load(getClass().getResource("/welcome.fxml")));
            scene.getStylesheets().add(getClass().getResource("/main.css").toExternalForm());
            stage.setTitle("Добро пожаловать");
            stage.setScene(scene);
            stage.setMaximized(true);                    // во весь экран
            stage.show();
        } catch (Exception ignored) {}
    }

    // ---- рисование ----
    private void redraw(DividerResult r) {
        var g = canvas.getGraphicsContext2D();
        clearCanvas();
        if (r == null) return;

        double w = canvas.getWidth(), h = canvas.getHeight();
        double margin = Math.max(40, Math.min(w, h) * 0.06);
        double left = margin, right = w - margin;
        double topY = margin + 40, botY = h - margin - 40;
        double midX = (left + right) / 2;

        g.setLineWidth(2);
        text(g, left - 25, topY - 30, "Vin");
        text(g, midX - 20, (topY + botY)/2 - 8, "Vout");
        text(g, right - 30, botY + 30, "GND");

        line(g, left, topY, right, topY);
        line(g, left, botY, right, botY);
        line(g, midX, topY, midX, botY);

        drawBranch(g, r.getTop(),    left + 80, topY,  right - 80, true);
        drawBranch(g, r.getBottom(), left + 80, botY, right - 80, false);

        text(g, left, topY - 12, "Верхнее плечо");
        text(g, left, botY + 22, "Нижнее плечо");
    }

    private void drawBranch(javafx.scene.canvas.GraphicsContext g, DividerResult.Branch br,
                            double x1, double y, double x2, boolean top) {
        double span = x2 - x1;
        double rw = Math.max(60, span * 0.18);
        double rh = 22;
        double gap = Math.max(18, span * 0.06);
        double labelOffset = top ? -8 : 14;

        if ("1".equals(br.type())) {
            double cx = (x1 + x2) / 2;
            drawRes(g, cx - rw/2, y - rh/2, rw, rh, org.example.calculator.model.DividerResult.fmtOhm(br.effective()), labelOffset);
        } else if ("S".equals(br.type())) {
            double cx1 = x1 + span*0.35, cx2 = x2 - span*0.35;
            drawRes(g, cx1 - rw/2, y - rh/2, rw, rh, org.example.calculator.model.DividerResult.fmtOhm(br.parts().get(0)), labelOffset);
            drawRes(g, cx2 - rw/2, y - rh/2, rw, rh, org.example.calculator.model.DividerResult.fmtOhm(br.parts().get(1)), labelOffset);
            line(g, cx1 + rw/2 + gap/2, y, cx2 - rw/2 - gap/2, y);
        } else { // "P"
            double cx = (x1 + x2) / 2, delta = 34;
            line(g, cx, y, cx, y + (top ? delta : -delta));
            drawRes(g, cx - rw - 8, y + (top ? 6 : -rh-6), rw, rh, org.example.calculator.model.DividerResult.fmtOhm(br.parts().get(0)), labelOffset);
            drawRes(g, cx + 8,        y + (top ? 6 : -rh-6), rw, rh, org.example.calculator.model.DividerResult.fmtOhm(br.parts().get(1)), labelOffset);
            line(g, cx, y + (top ? delta : -delta), cx, y);
        }
    }

    private void drawRes(javafx.scene.canvas.GraphicsContext g, double x, double y, double w, double h, String label, double labelOffset) {
        g.strokeRect(x, y, w, h);
        text(g, x + 2, y + labelOffset, label);
    }

    private void clearCanvas() {
        var g = canvas.getGraphicsContext2D();
        g.setFill(Color.WHITE); g.fillRect(0,0,canvas.getWidth(),canvas.getHeight());
        g.setFill(Color.BLACK);
    }
    private void line(javafx.scene.canvas.GraphicsContext g, double x1,double y1,double x2,double y2){ g.strokeLine(x1,y1,x2,y2); }
    private void text(javafx.scene.canvas.GraphicsContext g, double x,double y,String s){ g.fillText(s,x,y); }

    // utils
    private double parse(String s) {
        s = s==null ? "" : s.trim();
        s = s.replace(" ", "");
        if (s.chars().filter(ch -> ch==',').count() > 1) s = s.replace(",", "");
        else s = s.replace(",", ".");
        return Double.parseDouble(s);
    }
    private void clearOutput() {
        topologyLabel.setText(""); valuesLabel.setText("");
        voutLabel.setText(""); errLabel.setText("");
        countLabel.setText(""); powerLabel.setText("");
        clearCanvas();
    }
    private String fmt(double x) {
        String r = String.format(Locale.US,"%.6f", x);
        return r.replaceAll("0+$","").replaceAll("\\.$","");
    }
}
