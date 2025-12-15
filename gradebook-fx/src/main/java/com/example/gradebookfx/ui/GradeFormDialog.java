package com.example.gradebookfx.ui;

import com.example.gradebookfx.api.dto.GradeDto;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.util.Optional;

public class GradeFormDialog extends Dialog<GradeDto> {

    private final TextField studentName = new TextField();
    private final TextField subject = new TextField();
    private final Spinner<Integer> grade = new Spinner<>(0, 100, 0);

    private final Label errStudent = new Label();
    private final Label errSubject = new Label();
    private final Label errGrade = new Label();

    public GradeFormDialog(String title, GradeDto existing) {
        setTitle(title);
        setHeaderText(title);

        getDialogPane().getStyleClass().add("card");
        getDialogPane().getStylesheets().add(GradeFormDialog.class.getResource("/css/grades-fx.css").toExternalForm());

        ButtonType cancel = new ButtonType("Отменить", ButtonBar.ButtonData.CANCEL_CLOSE);
        ButtonType save = new ButtonType(existing == null ? "Добавить" : "Сохранить", ButtonBar.ButtonData.OK_DONE);

        getDialogPane().getButtonTypes().addAll(cancel, save);

        studentName.setPromptText("Иван Иванов");
        subject.setPromptText("Математика");

        grade.setEditable(true);
        grade.getEditor().setPromptText("95");

        errStudent.getStyleClass().add("error");
        errSubject.getStyleClass().add("error");
        errGrade.getStyleClass().add("error");

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.setPadding(new Insets(10, 4, 4, 4));

        // row 0
        grid.add(l("Студент *"), 0, 0);
        grid.add(studentName, 1, 0);
        grid.add(errStudent, 1, 1);

        // row 2
        grid.add(l("Предмет *"), 0, 2);
        grid.add(subject, 1, 2);
        grid.add(errSubject, 1, 3);

        // row 4
        grid.add(l("Оценка (0–100) *"), 0, 4);
        grid.add(grade, 1, 4);
        grid.add(errGrade, 1, 5);

        getDialogPane().setContent(grid);

        if (existing != null) {
            studentName.setText(nvl(existing.getStudentName()));
            subject.setText(nvl(existing.getSubject()));
            grade.getValueFactory().setValue(existing.getGrade() == null ? 0 : existing.getGrade());
        } else {
            grade.getValueFactory().setValue(95);
        }

        Node okButton = getDialogPane().lookupButton(save);
        okButton.disableProperty().bind(studentName.textProperty().isEmpty()
                .or(subject.textProperty().isEmpty()));

        setResultConverter(btn -> {
            if (btn != save) return null;

            clearErrors();
            Optional<String> err = validateForm();
            if (err.isPresent()) {
                return null;
            }

            GradeDto dto = new GradeDto();
            dto.setStudentName(studentName.getText().trim());
            dto.setSubject(subject.getText().trim());
            dto.setGrade(grade.getValue());
            return dto;
        });

        // hack: если ResultConverter вернул null при OK — диалог закроется.
        // Поэтому валидируем ещё раз на нажатии OK и отменяем close.
        okButton.addEventFilter(javafx.event.ActionEvent.ACTION, ev -> {
            clearErrors();
            if (validateForm().isPresent()) {
                ev.consume();
            }
        });
    }

    private Optional<String> validateForm() {
        boolean has = false;

        String s = studentName.getText() == null ? "" : studentName.getText().trim();
        String sub = subject.getText() == null ? "" : subject.getText().trim();
        Integer g = grade.getValue();

        if (s.isEmpty()) {
            errStudent.setText("Введите студента");
            has = true;
        }
        if (sub.isEmpty()) {
            errSubject.setText("Введите предмет");
            has = true;
        }
        if (g == null) {
            errGrade.setText("Введите оценку");
            has = true;
        } else if (g < 0 || g > 100) {
            errGrade.setText("Оценка должна быть 0–100");
            has = true;
        }

        return has ? Optional.of("invalid") : Optional.empty();
    }

    private void clearErrors() {
        errStudent.setText("");
        errSubject.setText("");
        errGrade.setText("");
    }

    private static Label l(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("field-label");
        return l;
    }

    private static String nvl(String s) {
        return s == null ? "" : s;
    }
}
