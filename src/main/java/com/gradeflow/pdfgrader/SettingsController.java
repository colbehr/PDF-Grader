package com.gradeflow.pdfgrader;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Paths;

import static com.gradeflow.pdfgrader.PDFGrader.getStage;
import static com.gradeflow.pdfgrader.PDFGrader.workingTest;

public class SettingsController {

    @FXML
        public void closeSettings(ActionEvent actionEvent) {
            Stage s = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            s.close();
    }

        public void openShortcuts(ActionEvent actionEvent) {
            String[] keywords = {"grading", "page"};
            Shortcuts.ShowShortcutDialog(keywords, "Grading Shortcuts");
        }

        public void SetExportFolder(ActionEvent actionEvent) {
            Export.browseAndSetExportFolder();
        }
}
