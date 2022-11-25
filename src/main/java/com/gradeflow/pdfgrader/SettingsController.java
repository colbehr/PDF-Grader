package com.gradeflow.pdfgrader;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Paths;

import static com.gradeflow.pdfgrader.GradingController.folderPath;
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

        public void openFolder(ActionEvent actionEvent) {
            //open a FileChooser when ChoosePDF is clicked
            DirectoryChooser folderChooser = new DirectoryChooser();
            folderChooser.setTitle("Choose a folder to save graded files");
            //Set initial directory to users Desktop
            folderChooser.setInitialDirectory(new File(System.getProperty("user.home") + System.getProperty("file.separator") + "Desktop"));
            File pdf = folderChooser.showDialog(getStage());
            if (pdf != null) {
                GradingController.setFolderPath(Paths.get(pdf.getPath()));
            }
        }

        public void selectFile(ActionEvent actionEvent) {
            FileChooser fileChooser = new FileChooser();
            FileChooser.ExtensionFilter pdfFilter = new FileChooser.ExtensionFilter("PDF files (*.pdf)", "*.pdf", "*.PDF");
            fileChooser.getExtensionFilters().add(pdfFilter);
            fileChooser.setTitle("Choose a location to save statistics overview");
            //Set initial directory to users Desktop
            fileChooser.setInitialDirectory(new File(System.getProperty("user.home") + System.getProperty("file.separator") + "Desktop"));
            if (workingTest != null) {
                fileChooser.setInitialFileName(workingTest.getName() + "_statistics.pdf");
            } else {
                fileChooser.setInitialFileName("grade_statistics.pdf");
            }
            File pdf = fileChooser.showSaveDialog(getStage());
            if (pdf != null) {
                GradingController.setFilePathText(Paths.get(pdf.getPath()));
            }
        }
}
