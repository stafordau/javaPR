package org.example.calculator.ui;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.calculator.model.DividerHistoryRow;
import org.example.calculator.service.HistoryDividerService;
import org.example.calculator.session.Session;

import java.util.Locale;

public class HistoryDividerController {

    @FXML private TextField searchField;
    @FXML private TableView<DividerHistoryRow> table;

    @FXML private TableColumn<DividerHistoryRow, String> loginCol, topoCol, valuesCol, voutCol, errCol, cntCol, powerCol, createdCol;

    private final HistoryDividerService svc = new HistoryDividerService();

    @FXML
    private void initialize() {
        var u = Session.get();
        boolean isAdmin = (u != null) && "ADMIN".equalsIgnoreCase(u.getRole());
        Integer userId = (u == null) ? null : u.getId();

        var data = FXCollections.observableArrayList(svc.load(isAdmin, userId));

        if (isAdmin) {
            loginCol.setCellValueFactory(r -> Bindings.createObjectBinding(() -> nvl(r.getValue().getLogin())));
        } else {
            table.getColumns().remove(loginCol); // скрываем колонку Пользователь
        }
        topoCol.setCellValueFactory(r -> Bindings.createObjectBinding(() -> r.getValue().getTopology()));
        valuesCol.setCellValueFactory(r -> Bindings.createObjectBinding(() -> r.getValue().getValues()));
        voutCol.setCellValueFactory(r -> Bindings.createObjectBinding(() -> fmt(r.getValue().getVout())));
        errCol.setCellValueFactory(r -> Bindings.createObjectBinding(() -> fmt(r.getValue().getErrPct())));
        cntCol.setCellValueFactory(r -> Bindings.createObjectBinding(() -> Integer.toString(r.getValue().getElements())));
        powerCol.setCellValueFactory(r -> Bindings.createObjectBinding(() -> fmt(r.getValue().getPowerMw())));
        createdCol.setCellValueFactory(r -> Bindings.createObjectBinding(() -> r.getValue().getCreated()));

        // глобальный поиск (по всем колонкам)
        FilteredList<DividerHistoryRow> filtered = new FilteredList<>(data, it -> true);
        searchField.textProperty().addListener((obs, o, n) -> {
            String needle = (n == null ? "" : n.trim().toLowerCase(Locale.ROOT));
            filtered.setPredicate(row -> {
                if (needle.isEmpty()) return true;
                return (nvl(row.getLogin()) + " " +
                        row.getTopology() + " " +
                        row.getValues() + " " +
                        fmt(row.getVout()) + " " +
                        fmt(row.getErrPct()) + " " +
                        row.getElements() + " " +
                        fmt(row.getPowerMw()) + " " +
                        row.getCreated()
                ).toLowerCase(Locale.ROOT).contains(needle);
            });
        });

        SortedList<DividerHistoryRow> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sorted);
    }

    @FXML
    private void onBack() {
        try {
            Stage stage = (Stage) table.getScene().getWindow();
            Scene scene = new Scene(FXMLLoader.load(getClass().getResource("/welcome.fxml")));
            scene.getStylesheets().add(getClass().getResource("/main.css").toExternalForm());
            stage.setTitle("Добро пожаловать");
            stage.setScene(scene);
            stage.setMaximized(true);                    // во весь экран
            stage.show();
        } catch (Exception ignored) {}
    }

    private String nvl(String s) { return s == null ? "" : s; }
    private String fmt(double d) {
        String s = String.format(java.util.Locale.US, "%.6f", d);
        s = s.replaceAll("0+$","").replaceAll("\\.$","");
        return s;
    }
}
