module com.example.gradebookfx {
    requires javafx.controls;
    requires javafx.fxml;

    requires java.net.http;

    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;

    opens com.example.gradebookfx.ui to javafx.fxml;
    opens com.example.gradebookfx.api.dto to com.fasterxml.jackson.databind;

    exports com.example.gradebookfx;
}
