package com.ezgrader.pdfgrader;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.TransferMode;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static com.ezgrader.pdfgrader.PDFGrader.getStage;

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
                    System.err.println(e);
                }
            }
        });

        // Run AFTER stage is created (which is after this init method)
        Platform.runLater(this::setupKeyboardShortcuts);
        Platform.runLater(this::setupDragNDrop);
        Platform.runLater(() -> getStage().setTitle("PDF Grader"));
    }

    private void setupDragNDrop() {
        PDFGrader.getStage().getScene().setOnDragOver(new EventHandler<DragEvent>() {
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
        PDFGrader.getStage().getScene().setOnDragDropped(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                Dragboard db = event.getDragboard();
                boolean success = false;
                if (db.hasFiles()) {
                    success = true;
                    String filePath = null;
                    List<File> pdfs = new ArrayList<>();
                    for (File file:db.getFiles()) {
                        filePath = file.getAbsolutePath();
                        int length = filePath.length();
                        //do the upload thing
                        if (filePath.substring(length - 4, length).equals(".pdf")) {
                            pdfs.add(file);
                        } else {
                            Toast.Error("Dragged file was not a pdf");
                        }
                    }
                    Path finalPath;
                    if (pdfs.size() > 1) {
                        finalPath = PDFGrader.MergePDFs(pdfs).toPath();
                    } else {
                        finalPath = Paths.get(filePath);
                    }
                    PDFGrader.workingTest = new Test(finalPath);
                    try {
                        PDFGrader.SwitchScene("setup.fxml", false);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                event.setDropCompleted(success);
                event.consume();
            }
        });
    }

    @FXML
    private void GoToSetup() throws IOException {
        PDFGrader.GoToSetup();
    }

    @FXML
    private void OpenTest() throws IOException {
        PDFGrader.OpenTest();
    }

    private void setupKeyboardShortcuts() {
        PDFGrader.getStage().getScene().addEventFilter(KeyEvent.KEY_PRESSED, ke -> {
            if (Shortcuts.get("homeNew").match(ke)) {
                try {
                    GoToSetup();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                ke.consume();
            } else if (Shortcuts.get("homeOpen").match(ke)) {
                try {
                    OpenTest();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                ke.consume();
            }
        });
    }

    @FXML
    private void ShowShortcutDialog() {
        String[] keywords = { "home", "page" };
        Shortcuts.ShowShortcutDialog(keywords, "Home Shortcuts");
    }
}
