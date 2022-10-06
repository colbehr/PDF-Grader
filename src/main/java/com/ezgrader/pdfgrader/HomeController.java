package com.ezgrader.pdfgrader;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.stage.FileChooser;
import org.apache.pdfbox.contentstream.operator.state.Save;

import java.awt.dnd.DropTarget;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
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
            String name = data.getValue().substring(data.getValue().lastIndexOf("\\") + 1);
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
    private void DragNDrop() {
        Group root = new Group();
        Scene scene = recentTable.getScene();
        scene.setOnDragOver(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                Dragboard db = event.getDragboard();
                if (db.hasFiles()) {
                    event.acceptTransferModes(TransferMode.COPY);
                } else {
                    event.consume();
                }
            }
        });

        // Dropping over surface
        scene.setOnDragDropped(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                Dragboard db = event.getDragboard();
                boolean success = false;
                if (db.hasFiles()) {
                    success = true;
                    String filePath = null;
                    for (File file:db.getFiles()) {
                        filePath = file.getAbsolutePath();
                        int length = filePath.length();
                        //do the upload thing
                        if (filePath.substring(length - 4, length).equals(".pdf")) {
                            Path finalPath = Paths.get(filePath);
                            PDFGrader.workingTest = new Test(finalPath);
                            try {
                                PDFGrader.SwitchScene("setup.fxml", false);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        } else {
                            //need to add a popup that says not a pdf
                            System.out.println("nah");
                        }
                    }
                }
                event.setDropCompleted(success);
                event.consume();
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
