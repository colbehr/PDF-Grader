package com.gradeflow.pdfgrader;

import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.TransferMode;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static com.gradeflow.pdfgrader.PDFGrader.getStage;

public class HomeController {
    @FXML
    private TableView recentTable;
    @FXML
    private TableColumn<String, String> pathCol;
    @FXML
    private TableColumn<String, String> nameCol;
    @FXML
    private TextField searchTextField;
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
        Platform.runLater(this::setupSearch);
        // recentTable.setItems(recentTests);

        // Make recent tests open on click
        recentTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                try {
                    File file = new File(obs.getValue().toString());
                    PDFGrader.workingTest = SaveLoad.LoadTest(file);
                    PDFGrader.SwitchScene("grading.fxml", false);
                } catch (IOException e) {
                    Toast.Error("Error loading test " + obs.getValue().toString());
                    System.err.println("Error loading recent test");
                    System.err.println(e);
                }
            }
        });

        // Run AFTER stage is created (which is after this init method)
        Platform.runLater(this::setupKeyboardShortcuts);
        Platform.runLater(this::setupDragNDrop);
        Platform.runLater(() -> {
            getStage().setTitle("Gradeflow");
            getStage().getIcons().add(new Image(getClass().getResourceAsStream("img/journals-square.png")));
        }); // reset title
    }

    /**
     * Initializes functionality to drop PDF anywhere on home page to open it
     */
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

    /**
     * Initializes search for recent tests table
     */
    private void setupSearch() {
        // Sample code from:
        // https://code.makery.ch/blog/javafx-8-tableview-sorting-filtering/

        // 1. Wrap the ObservableList in a FilteredList (initially display all data).
        FilteredList<String> filteredData = new FilteredList<>(recentTests, p -> true);

        // 2. Set the filter Predicate whenever the filter changes.
        searchTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(s -> {
                // If filter text is empty, display all persons.
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                // Compare first name and last name of every person with filter text.
                String lowerCaseFilter = newValue.toLowerCase();

                if (s.toLowerCase().contains(lowerCaseFilter)) {
                    return true; // Filter matches first name.
                }
                return false; // Does not match.
            });
        });

        // 3. Wrap the FilteredList in a SortedList.
        SortedList<String> sortedData = new SortedList<>(filteredData);

        // 4. Bind the SortedList comparator to the TableView comparator.
        sortedData.comparatorProperty().bind(recentTable.comparatorProperty());

        // 5. Add sorted (and filtered) data to the table.
        recentTable.setItems(sortedData);
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
        stage.setX(getStage().getX()+230);
        stage.setY(getStage().getY()+50.0);
        Timeline tick = new Timeline();
        tick.setCycleCount(Timeline.INDEFINITE);

        stage.show();
    }

    @FXML
    private void ShowShortcutDialog() {
        String[] keywords = { "home", "page" };
        Shortcuts.ShowShortcutDialog(keywords, "Home Shortcuts");
    }

    @FXML
    private void showAbout(){
        PDFGrader.showAboutPage();
    }

}
