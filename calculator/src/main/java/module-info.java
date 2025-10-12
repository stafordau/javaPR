module org.example.calculator {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires jbcrypt;

    exports org.example.calculator;
    exports org.example.calculator.model;
    opens org.example.calculator.ui to javafx.fxml; // ОБЯЗАТЕЛЬНО
}
