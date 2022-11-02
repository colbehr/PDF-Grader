package com.ezgrader.pdfgrader;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.*;
import javafx.stage.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

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

        getStage().setMinWidth(660);
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

        // Run AFTER stage is created (which is after this init method)
        Platform.runLater(this::setupKeyboardShortcuts);
        Platform.runLater(this::setupDragNDrop);
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

    public void settingsFrame() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("settings.fxml"));
        Parent root1 = (Parent) fxmlLoader.load();
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setTitle("Settings");
        stage.setScene(new Scene(root1));
        stage.setX(getStage().getX()+200.0);
        stage.setY(getStage().getY()+40.0);
        stage.show();
    }

    @FXML
    private void ShowShortcutDialog() {
        String[] keywords = { "home", "page" };
        Shortcuts.ShowShortcutDialog(keywords, "Home Shortcuts");
    }
}
