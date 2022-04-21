module com.ezgrader.pdfgrader {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires javafx.swing;
    requires org.apache.pdfbox;
    requires java.desktop;


    opens com.ezgrader.pdfgrader to javafx.fxml;
    exports com.ezgrader.pdfgrader;
}