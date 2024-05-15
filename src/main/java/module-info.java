module IdeMZ {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires org.fxmisc.richtext;
    requires org.apache.logging.log4j;

    opens com.IdeMZ to javafx.fxml;
    exports com.IdeMZ to javafx.graphics;
}