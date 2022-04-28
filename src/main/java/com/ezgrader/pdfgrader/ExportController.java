package com.ezgrader.pdfgrader;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.ezgrader.pdfgrader.PDFGrader.workingTest;

public class ExportController {

    private Path statisticsPath;
    private Path folderPath;

    @FXML
    private javafx.scene.control.Label folderPathText;
    @FXML
    private javafx.scene.control.Label filePathText;


    /**
     * opens a browser to find a folder to put tests into
     * @param event
     */
    @FXML
    private void browseForTestFolder(ActionEvent event) {
        //open a FileChooser when ChoosePDF is clicked
        DirectoryChooser folderChooser = new DirectoryChooser();
        folderChooser.setTitle("Choose a folder to save graded files");
        //Set initial directory to users Desktop
        folderChooser.setInitialDirectory(new File(System.getProperty("user.home") + System.getProperty("file.separator")+ "Desktop"));
        File pdf = folderChooser.showDialog(((Node) event.getSource()).getScene().getWindow());
        folderPath = Paths.get(pdf.getPath());
        folderPathText.setText(folderPath.toString());
    }

    /**
     * Opens a new file chooser for saving a statistics file
     * @param event
     */
    @FXML
    private void browseForStatistics(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter pdfFilter = new FileChooser.ExtensionFilter("PDF files (*.pdf)", "*.pdf", "*.PDF");
        fileChooser.getExtensionFilters().add(pdfFilter);
        fileChooser.setTitle("Choose a location to save statistics overview");
        //Set initial directory to users Desktop
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home") + System.getProperty("file.separator")+ "Desktop"));
        if (workingTest != null) {
            fileChooser.setInitialFileName(workingTest.getName() + "_statistics.pdf");
        } else{
            fileChooser.setInitialFileName("test_statistics.pdf");
        }
        File pdf = fileChooser.showSaveDialog(((Node) event.getSource()).getScene().getWindow());
        statisticsPath = Paths.get(pdf.getPath());
        filePathText.setText(statisticsPath.toString());
    }

    /**
     * exports files when export button is pressed
     * @param event
     */
    @FXML
    private void exportFiles(ActionEvent event) {
        System.out.println("Exported statistics");
        //TODO: export statistics
        System.out.println("Exported students tests");
        //TODO: export tests
        //open dialog, return to home
        Dialog dialog = new Dialog();
        ButtonType okButton = new ButtonType("Ok", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().add(okButton);
        dialog.setTitle("Finished Exporting");
        dialog.setContentText("Finished exporting statistics and tests.");
        dialog.showAndWait()
                .filter(response -> response == ButtonType.OK);
        try {
            PDFGrader.SwitchScene("home.fxml");
        } catch (IOException e) {
            System.exit(0);
        }
    }
}
