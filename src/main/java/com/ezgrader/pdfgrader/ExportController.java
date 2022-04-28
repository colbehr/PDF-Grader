package com.ezgrader.pdfgrader;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
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
        if (statisticsPath == null || folderPath == null){
            String errorMessage = "";
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Path has not been set");
            if (statisticsPath == null){
                errorMessage = "Please find a location for the statistics file.";
            }
            if (folderPath == null){
                errorMessage += "\n\nPlease find a folder for graded files.";
            }
            alert.setContentText(errorMessage);
            alert.showAndWait();
            return;
        }
        System.out.println("Exported statistics");
        //TODO: export statistics
        System.out.println("Exported students tests");
        //TODO: export tests
        //open dialog, return to home

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Finished Exporting");
        alert.setHeaderText("Files exported.");
        alert.setContentText("Statistics exported to \n" + statisticsPath.toString() + "\n\nFiles exported to \n" + folderPath.toString());
        alert.showAndWait();

        try {
            PDFGrader.SwitchScene("home.fxml");
        } catch (IOException e) {
            System.exit(0);
        }
    }
}
