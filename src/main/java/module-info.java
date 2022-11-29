module com.gradeflow.pdfgrader {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires javafx.swing;
    requires org.apache.pdfbox;
    requires java.desktop;
    requires org.json;


    opens com.gradeflow.pdfgrader to javafx.fxml;
    exports com.gradeflow.pdfgrader;
}