package com.ezgrader.pdfgrader;

import javafx.fxml.FXML;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

public class HomeController {
    @FXML
    public void initialize() {

    }

    @FXML
    private void GoToSetup() throws IOException {
        FileChooser.ExtensionFilter pdfFilter = new FileChooser.ExtensionFilter("PDF files (*.pdf)", "*.pdf", "*.PDF");
        File pdf = PDFGrader.OpenFileChooser("Choose PDF", pdfFilter);

        if (pdf != null) {
            PDFGrader.workingTest = new Test(Paths.get(pdf.getPath()));
            PDFGrader.SwitchScene("setup.fxml", false);
        }
    }

    @FXML
    private void OpenTest() throws IOException {
        FileChooser.ExtensionFilter jsonFilter = new FileChooser.ExtensionFilter("JSON files (*.json)", "*.json", "*.JSON");
        //Set initial directory to users downloads
        File testFile = PDFGrader.OpenFileChooser("Choose Previously Created Test", jsonFilter);
        if (testFile != null) {
            PDFGrader.workingTest = SaveLoad.LoadTest(testFile);
            PDFGrader.SwitchScene("grading.fxml", false);
        }
    }
}
