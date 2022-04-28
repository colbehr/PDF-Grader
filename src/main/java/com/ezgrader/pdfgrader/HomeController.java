package com.ezgrader.pdfgrader;

import javafx.fxml.FXML;

import java.io.IOException;

public class HomeController {
    @FXML
    public void initialize() {

    }

    @FXML
    private void GoToSetup() throws IOException {
        PDFGrader.SwitchScene("setup.fxml");
    }
}
