package com.ezgrader.pdfgrader;

import javafx.fxml.FXML;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;

public class HomeController {
    @FXML
    public void initialize() {

    }

    @FXML
    private void GoToSetup() throws IOException {
        PDFGrader.SwitchScene("setup.fxml", false);
    }

    @FXML
    private void OpenTest() throws IOException {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter jsonFilter = new FileChooser.ExtensionFilter("JSON files (*.json)", "*.json", "*.JSON");
        fileChooser.getExtensionFilters().add(jsonFilter);
        fileChooser.setTitle("Choose Previously Created Test");
        //Set initial directory to users downloads
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home") + System.getProperty("file.separator")+ "Downloads"));
        File testFile = fileChooser.showOpenDialog(PDFGrader.getStage().getScene().getWindow());
        if (testFile != null) {
            PDFGrader.workingTest = SaveLoad.LoadTest(testFile);
            PDFGrader.SwitchScene("grading.fxml");
        }
    }
}
