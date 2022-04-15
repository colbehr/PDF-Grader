package com.ezgrader.pdfgrader;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;

import java.io.File;

public class SetupController {
    private File pdf;

    @FXML
    private Label pdfFilename;
    @FXML
    private TextField pagesField;

    @FXML
    public void initialize() {
        pagesField.setTextFormatter(TextFilters.GetIntFilter());
    }

    @FXML
    private void browseForPDF(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter pdfFilter = new FileChooser.ExtensionFilter("PDF files (*.pdf)", "*.pdf");
        FileChooser.ExtensionFilter swagFilter = new FileChooser.ExtensionFilter("Your mom lol", "*.dab");
        fileChooser.getExtensionFilters().add(pdfFilter);
        fileChooser.getExtensionFilters().add(swagFilter);
        fileChooser.setTitle("Choose PDF");
        pdf = fileChooser.showOpenDialog(((Node)event.getSource()).getScene().getWindow());
        if (pdf != null) {
            pdfFilename.setText(pdf.getName());
        }
    }
}
