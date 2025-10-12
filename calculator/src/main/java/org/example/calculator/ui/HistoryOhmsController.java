package org.example.calculator.ui;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.calculator.model.OhmsRow;
import org.example.calculator.session.Session;
import org.example.calculator.service.HistoryOhmsService;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

public class HistoryOhmsController {

    @FXML private TableView<OhmsRow> table;

    private final HistoryOhmsService svc = new HistoryOhmsService();
    private final Map<TableColumn<OhmsRow, ?>, String> filters = new HashMap<>();
    private FilteredList<OhmsRow> filtered;

    @FXML
    private void initialize() {
        var u = Session.get();
        boolean isAdmin = (u != null) && "ADMIN".equalsIgnoreCase(u.getRole());
        Integer uid = (u == null) ? null : u.getId();

        List<OhmsRow> rows = svc.loadOhms(isAdmin, uid);
        ObservableList<OhmsRow> data = FXCollections.observableArrayList(rows);

        // Колонки
        if (isAdmin) {
            var colLogin = mkCol("Пользователь", OhmsRow::getLogin);
            table.getColumns().add(colLogin);
        }


        var colI  = mkCol("I", OhmsRow::getIValue);
        var colIu = mkCol("I, ед.", OhmsRow::getIUnit);
        var colV  = mkCol("V", OhmsRow::getVValue);
        var colVu = mkCol("V, ед.", OhmsRow::getVUnit);
        var colR  = mkCol("R", OhmsRow::getRValue);
        var colRu = mkCol("R, ед.", OhmsRow::getRUnit);

        table.getColumns().addAll(colI, colIu, colV, colVu, colR, colRu);

        // Включаем сортировку по клику (дефолтная JavaFX)
        table.getSortOrder().clear();

        // Фильтрация — суммарный предикат по всем колонкам
        filtered = new FilteredList<>(data, r -> true);
        SortedList<OhmsRow> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sorted);

        // Подключаем контекстное меню с поиском к каждой колонке
        table.getColumns().forEach(this::installFilterMenu);
    }

    // Создание TableColumn (универсальный вариант и для String и для чисел)
    private <T> TableColumn<OhmsRow, T> mkCol(String title, Function<OhmsRow, T> getter) {
        TableColumn<OhmsRow, T> c = new TableColumn<>(title);
        c.setCellValueFactory(param -> Bindings.createObjectBinding(() -> getter.apply(param.getValue())));
        c.setSortable(true);
        c.setPrefWidth(110);
        return c;
    }

    private void installFilterMenu(TableColumn<OhmsRow, ?> col) {
        ContextMenu menu = new ContextMenu();

        MenuItem sortAsc = new MenuItem("Сортировать ↑");
        sortAsc.setOnAction(e -> { col.setSortType(TableColumn.SortType.ASCENDING); if(!table.getSortOrder().contains(col)) table.getSortOrder().add(col); table.sort(); });

        MenuItem sortDesc = new MenuItem("Сортировать ↓");
        sortDesc.setOnAction(e -> { col.setSortType(TableColumn.SortType.DESCENDING); if(!table.getSortOrder().contains(col)) table.getSortOrder().add(col); table.sort(); });

        TextField search = new TextField();
        search.setPromptText("Поиск");
        search.setMinWidth(160);
        search.textProperty().addListener((obs, o, n) -> {
            filters.put(col, n == null ? "" : n.trim());
            recomputePredicate();
        });
        // текущий фильтр в поле
        search.setText(filters.getOrDefault(col, ""));

        CustomMenuItem searchItem = new CustomMenuItem(search, false);

        MenuItem clear = new MenuItem("Очистить фильтр");
        clear.setOnAction(e -> { filters.remove(col); recomputePredicate(); });

        menu.getItems().addAll(sortAsc, sortDesc, new SeparatorMenuItem(), searchItem, clear);

        // показываем меню по правому клику на заголовке
        col.setContextMenu(menu);
    }

    private void recomputePredicate() {
        filtered.setPredicate(row -> {
            for (var entry : filters.entrySet()) {
                TableColumn<OhmsRow, ?> c = entry.getKey();
                String needle = entry.getValue();
                if (needle == null || needle.isEmpty()) continue;

                String hay = getCellText(c, row).toLowerCase(Locale.ROOT);
                if (!hay.contains(needle.toLowerCase(Locale.ROOT))) return false;
            }
            return true;
        });
    }

    // Получение текстового значения ячейки для фильтра
    private String getCellText(TableColumn<OhmsRow, ?> col, OhmsRow r) {
        String name = col.getText();
        return switch (name) {
            case "Пользователь" -> nvl(r.getLogin());
            case "I" -> fmt(r.getIValue());
            case "I, ед." -> nvl(r.getIUnit());
            case "V" -> fmt(r.getVValue());
            case "V, ед." -> nvl(r.getVUnit());
            case "R" -> fmt(r.getRValue());
            case "R, ед." -> nvl(r.getRUnit());
            default -> "";
        };
    }

    private String nvl(String s) { return s == null ? "" : s; }
    private String fmt(double d) {
        String s = String.format(Locale.US, "%.6f", d);
        s = s.replaceAll("0+$","").replaceAll("\\.$","");
        return s.isEmpty() ? "0" : s;
    }

    @FXML
    private void onResetFilters() {
        filters.clear();
        recomputePredicate();
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
}
