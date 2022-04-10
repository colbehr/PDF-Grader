module com.ezgrader.pdfgrader {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;


    opens com.ezgrader.pdfgrader to javafx.fxml;
    exports com.ezgrader.pdfgrader;
}