package com.ezgrader.pdfgrader;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.FileChooser;
import org.apache.pdfbox.contentstream.operator.state.Save;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

public class HomeController {
    @FXML
    private TableView recentTable;
    @FXML
    private TableColumn<String, String> pathCol;
    @FXML
    private TableColumn<String, String> nameCol;
    private ObservableList<String> recentTests;
    @FXML
    public void initialize() throws IOException {
        recentTests = FXCollections.observableArrayList();
        recentTests.addAll(SaveLoad.GetRecentTests());
        pathCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()));
        nameCol.setCellValueFactory(data -> {
            String name = data.getValue().substring(data.getValue().lastIndexOf("\\")+1);
            return new SimpleStringProperty(name);
        });
        recentTable.setItems(recentTests);

        // Make recent tests open on click
        recentTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                try {
                    File file = new File(obs.getValue().toString());
                    PDFGrader.workingTest = SaveLoad.LoadTest(file);
                    PDFGrader.SwitchScene("grading.fxml", false);
                } catch (IOException e) {
                    System.err.println("Error loading recent test");
                }
            }
        });
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
            System.out.println(testFile.getAbsolutePath());
            PDFGrader.workingTest = SaveLoad.LoadTest(new File(testFile.getAbsolutePath()));
            PDFGrader.SwitchScene("grading.fxml", false);
        }
    }
}
