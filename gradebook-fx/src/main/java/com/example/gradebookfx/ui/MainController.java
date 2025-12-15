package com.example.gradebookfx.ui;

import com.example.gradebookfx.api.ApiClient;
import com.example.gradebookfx.api.dto.GradeDto;
import javafx.application.Platform;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.util.Comparator;
import java.util.List;

public class MainController {

    @FXML private Label statusLabel;
    @FXML private TableView<GradeDto> table;
    @FXML private Button addButton;
    @FXML private Button refreshButton;

    private final ApiClient api = new ApiClient();
    private final ObservableList<GradeDto> items = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        table.setItems(items);
        initColumns();
        wireActions();
        refresh();
    }

    private void initColumns() {
        TableColumn<GradeDto, Number> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(c -> new SimpleLongProperty(c.getValue().getId() == null ? 0L : c.getValue().getId()));
        idCol.setPrefWidth(80);

        TableColumn<GradeDto, String> studentCol = new TableColumn<>("Студент");
        studentCol.setCellValueFactory(c -> new SimpleStringProperty(nvl(c.getValue().getStudentName())));
        studentCol.setPrefWidth(260);

        TableColumn<GradeDto, String> subjectCol = new TableColumn<>("Предмет");
        subjectCol.setCellValueFactory(c -> new SimpleStringProperty(nvl(c.getValue().getSubject())));
        subjectCol.setPrefWidth(240);

        TableColumn<GradeDto, String> gradeCol = new TableColumn<>("Оценка");
        gradeCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getGrade() == null ? "" : String.valueOf(c.getValue().getGrade())));
        gradeCol.setPrefWidth(120);
        gradeCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<GradeDto, Void> actions = new TableColumn<>("Действия");
        actions.setPrefWidth(240);
        actions.setStyle("-fx-alignment: CENTER;");
        actions.setCellFactory(col -> new TableCell<>() {
            private final Button edit = new Button("Редактировать");
            private final Button del = new Button("Удалить");
            private final HBox box = new HBox(8, edit, del);

            {
                edit.getStyleClass().addAll("btn", "btn-ghost");
                del.getStyleClass().addAll("btn", "btn-danger");
                box.getStyleClass().add("row-actions");

                edit.setOnAction(e -> {
                    GradeDto dto = getTableView().getItems().get(getIndex());
                    openEdit(dto);
                });

                del.setOnAction(e -> {
                    GradeDto dto = getTableView().getItems().get(getIndex());
                    delete(dto);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });

        table.getColumns().setAll(idCol, studentCol, subjectCol, gradeCol, actions);
        table.getSortOrder().setAll(idCol);
        idCol.setSortType(TableColumn.SortType.ASCENDING);
    }

    private void wireActions() {
        addButton.setOnAction(e -> openCreate());
        refreshButton.setOnAction(e -> refresh());

        table.setRowFactory(tv -> {
            TableRow<GradeDto> row = new TableRow<>();
            row.setOnMouseClicked(ev -> {
                if (ev.getClickCount() == 2 && !row.isEmpty()) {
                    openEdit(row.getItem());
                }
            });
            return row;
        });
    }

    private void refresh() {
        setBusy(true, "Загрузка...");
        new Thread(() -> {
            try {
                List<GradeDto> grades = api.listGrades();
                grades.sort(Comparator.comparing(g -> g.getId() == null ? 0L : g.getId()));
                Platform.runLater(() -> {
                    items.setAll(grades);
                    setBusy(false, "Готово");
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    setBusy(false, "Ошибка загрузки");
                    Alerts.error("Не удалось загрузить список", ex.getMessage());
                });
            }
        }, "load-grades").start();
    }

    private void openCreate() {
        GradeFormDialog dialog = new GradeFormDialog("Добавить оценку", null);
        dialog.showAndWait().ifPresent(dto -> {
            setBusy(true, "Сохранение...");
            new Thread(() -> {
                try {
                    GradeDto created = api.createGrade(dto);
                    Platform.runLater(() -> {
                        items.add(created);
                        table.sort();
                        setBusy(false, "Добавлено");
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> {
                        setBusy(false, "Ошибка");
                        Alerts.error("Не удалось добавить", ex.getMessage());
                    });
                }
            }, "create-grade").start();
        });
    }

    private void openEdit(GradeDto original) {
        if (original == null || original.getId() == null) return;

        GradeFormDialog dialog = new GradeFormDialog("Редактировать оценку", original);
        dialog.showAndWait().ifPresent(dto -> {
            setBusy(true, "Сохранение...");
            new Thread(() -> {
                try {
                    GradeDto updated = api.updateGrade(original.getId(), dto);
                    Platform.runLater(() -> {
                        int idx = findIndexById(updated.getId());
                        if (idx >= 0) items.set(idx, updated);
                        table.sort();
                        setBusy(false, "Сохранено");
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> {
                        setBusy(false, "Ошибка");
                        Alerts.error("Не удалось сохранить", ex.getMessage());
                    });
                }
            }, "update-grade").start();
        });
    }

    private void delete(GradeDto dto) {
        if (dto == null || dto.getId() == null) return;

        boolean ok = Alerts.confirm("Удалить запись?", "Удалить оценку ID=" + dto.getId() + "?");
        if (!ok) return;

        setBusy(true, "Удаление...");
        new Thread(() -> {
            try {
                api.deleteGrade(dto.getId());
                Platform.runLater(() -> {
                    items.removeIf(x -> x.getId() != null && x.getId().equals(dto.getId()));
                    setBusy(false, "Удалено");
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    setBusy(false, "Ошибка");
                    Alerts.error("Не удалось удалить", ex.getMessage());
                });
            }
        }, "delete-grade").start();
    }

    private int findIndexById(Long id) {
        if (id == null) return -1;
        for (int i = 0; i < items.size(); i++) {
            if (id.equals(items.get(i).getId())) return i;
        }
        return -1;
    }

    private void setBusy(boolean busy, String status) {
        statusLabel.setText(status == null ? "" : status);
        addButton.setDisable(busy);
        refreshButton.setDisable(busy);
        table.setDisable(busy);
    }

    private static String nvl(String s) {
        return s == null ? "" : s;
    }
}
