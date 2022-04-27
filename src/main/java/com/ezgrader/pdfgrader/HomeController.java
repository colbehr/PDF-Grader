package com.ezgrader.pdfgrader;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;

import java.io.IOException;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HomeController {
    @FXML
    public void initialize() {

    }

    @FXML
    private void GoToSetup() throws IOException {
        Main.SwitchScene("setup.fxml");
    }
}
