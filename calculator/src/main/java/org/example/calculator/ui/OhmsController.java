package org.example.calculator.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.calculator.session.Session;
import org.example.calculator.service.OhmsService;

public class OhmsController {

    @FXML private TextField vField, iField, rField;
    @FXML private ComboBox<String> vUnit, iUnit, rUnit;

    private final OhmsService service = new OhmsService();

    @FXML
    private void initialize() {
        vUnit.getItems().addAll("В", "мВ");
        iUnit.getItems().addAll("А", "мА");
        rUnit.getItems().addAll("Ом", "кОм", "МОм");
        vUnit.getSelectionModel().select("В");
        iUnit.getSelectionModel().select("А");
        rUnit.getSelectionModel().select("Ом");

        // авто-блокировка третьего поля: как только заполнены любые два
        var listener = (javafx.beans.value.ChangeListener<String>) (obs, oldVal, newVal) -> updateDisabledFields();
        vField.textProperty().addListener(listener);
        iField.textProperty().addListener(listener);
        rField.textProperty().addListener(listener);
        updateDisabledFields();
    }

    private void updateDisabledFields() {
        int filled = (isFilled(vField)?1:0) + (isFilled(iField)?1:0) + (isFilled(rField)?1:0);
        if (filled >= 2) {
            // блокируем единственное пустое
            vField.setDisable(!isFilled(vField) && filled>=2);
            iField.setDisable(!isFilled(iField) && filled>=2);
            rField.setDisable(!isFilled(rField) && filled>=2);
        } else {
            vField.setDisable(false);
            iField.setDisable(false);
            rField.setDisable(false);
        }
    }

    private boolean isFilled(TextField tf) {
        String t = tf.getText();
        return t != null && !t.trim().isEmpty();
    }

    @FXML
    private void onCalculate() {
        try {
            Double V = parseOrNull(vField.getText());
            Double I = parseOrNull(iField.getText());
            Double R = parseOrNull(rField.getText());

            if ((V==null?0:1) + (I==null?0:1) + (R==null?0:1) != 2)
                throw new IllegalArgumentException("Введите ровно ДВА значения");

            // -> базовые единицы (В, А, Ом)
            Double Vb = (V==null)?null : ("мВ".equals(vUnit.getValue()) ? V/1000.0 : V);
            Double Ib = (I==null)?null : ("мА".equals(iUnit.getValue()) ? I/1000.0 : I);
            Double Rb = (R==null)?null : switch (rUnit.getValue()) {
                case "кОм" -> R*1_000.0;
                case "МОм" -> R*1_000_000.0;
                default -> R;
            };

            // вычисление отсутствующего
            String computed; // что именно посчитали
            if (Vb == null) { Vb = Ib * Rb; computed = "V"; }
            else if (Ib == null) { Ib = Vb / Rb; computed = "I"; }
            else { Rb = Vb / Ib; computed = "R"; }

            // переведём для отображения в выбранные юниты (но в поля НЕ пишем)
            double Vout = "мВ".equals(vUnit.getValue()) ? Vb*1000.0 : Vb;
            double Iout = "мА".equals(iUnit.getValue()) ? Ib*1000.0 : Ib;
            double Rout = switch (rUnit.getValue()) {
                case "кОм" -> Rb/1_000.0;
                case "МОм" -> Rb/1_000_000.0;
                default -> Rb;
            };

            // всплывающее окно с исходными и результатом
            String summary = """
                Напряжение: %s %s
                Ток:        %s %s
                Сопротивл.: %s %s

                Вычислено: %s
                """.formatted(
                    V==null ? "(расчитано) "+trimNum(Vout) : trimNum(Vout), vUnit.getValue(),
                    I==null ? "(расчитано) "+trimNum(Iout) : trimNum(Iout), iUnit.getValue(),
                    R==null ? "(расчитано) "+trimNum(Rout) : trimNum(Rout), rUnit.getValue(),
                    computed
            );
            new Alert(Alert.AlertType.INFORMATION) {{
                setHeaderText("Результат расчёта");
                setContentText(summary);
            }}.showAndWait();

            // сохраняем (как на экране)
            var u = Session.get();
            Integer userId = (u==null) ? null : u.getId();
            service.saveOhmsRecord(
                    userId,
                    Vout, vUnit.getValue(),
                    Iout, iUnit.getValue(),
                    Rout, rUnit.getValue()
            );

            // поля НЕ трогаем — остаются как вводились; блокировка сохраняется
            // updateDisabledFields(); // можно оставить, если нужно пересчитать блокировку

        } catch (NumberFormatException nfe) {
            showErr("Ввод", "Используйте число (точка как разделитель)");
        } catch (IllegalArgumentException iae) {
            showErr("Проверка данных", iae.getMessage());
        } catch (Exception ex) {
            showErr("Ошибка", "Не удалось выполнить расчёт");
        }
    }


    private String trimNum(double x) {
        String s = String.format(java.util.Locale.US, "%.6f", x);
        s = s.replaceAll("0+$", "").replaceAll("\\.$", "");
        return s.isEmpty() ? "0" : s;
    }

    private Double parseOrNull(String t) {
        if (t==null) return null;
        t = t.trim();
        if (t.isEmpty()) return null;
        return Double.parseDouble(t.replace(",", "."));
    }

    @FXML
    private void onBack() {
        try {
            Stage stage = (Stage) vField.getScene().getWindow();
            Scene scene = new Scene(FXMLLoader.load(getClass().getResource("/welcome.fxml")));
            scene.getStylesheets().add(getClass().getResource("/main.css").toExternalForm());

            stage.setTitle("Добро пожаловать");
            stage.setScene(scene);
            stage.setMaximized(true);                    // во весь экран
            stage.show();
        } catch (Exception ignored) { }
    }

    private void showErr(String head, String msg) {
        new Alert(Alert.AlertType.ERROR) {{ setHeaderText(head); setContentText(msg); }}.showAndWait();
    }
}
