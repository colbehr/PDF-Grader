package com.ezgrader.pdfgrader;


import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.security.Key;
import java.util.ArrayList;
import java.util.Map;

public class PDFGrader extends Application {

    private static final int MIN_WIDTH = 300;
    private static final int MIN_HEIGHT = 500;

    public static Test workingTest;
    private static Stage stage;

    @Override
    public void start(Stage primaryStage) throws Exception{
        stage = primaryStage;
        stage.setTitle("PDF Grader");
        stage.setMinWidth(MIN_WIDTH);
        stage.setMinHeight(MIN_HEIGHT);
        SwitchScene("home.fxml", false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        try {
            launch(args);
        }catch(NullPointerException e)   {
            System.out.println("NullPointerException error has occurred");
        }
    }

    /**
     * Allows a scene with the given root to be fully resizable
     * @param root
     */
    public static void MakeStretchy(GridPane root) {
        ColumnConstraints col = new ColumnConstraints();
        col.setPercentWidth(100);
        root.getColumnConstraints().add(col);
        RowConstraints row = new RowConstraints();
        row.setPercentHeight(100);
        root.getRowConstraints().add(row);
    }

    /**
     * Switches the application stage to display a new scene
     * @param sceneName
     * @param maintainSize
     * @throws IOException
     */
    public static void SwitchScene(String sceneName, boolean maintainSize) throws IOException {
        try {
            AnchorPane newRoot = FXMLLoader.load(PDFGrader.class.getResource(sceneName));
//            MakeStretchy(newRoot);

            if (maintainSize || stage.isMaximized()) {
                // force maintaining window size
                newRoot.setMinSize(stage.getWidth(), stage.getHeight());
                newRoot.setMaxSize(stage.getWidth(), stage.getHeight());
            }
            stage.setScene(new Scene(newRoot));
            // set back
            newRoot.setMinSize(MIN_WIDTH, MIN_HEIGHT);
            newRoot.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        }catch(NullPointerException e){
            System.out.println("NullPointerException error has occured");
        }

    }

    public static void SwitchScene(String sceneName) throws IOException {
        SwitchScene(sceneName, true);
    }

    public static Stage getStage() {
        return stage;
    }

    public static File OpenFileChooser(String title, FileChooser.ExtensionFilter filter) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(filter);
        fileChooser.setTitle(title);
        //Set initial directory to users downloads
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home") + System.getProperty("file.separator")+ "Downloads"));
        return fileChooser.showOpenDialog(PDFGrader.getStage().getScene().getWindow());
    }

    public static void SetWorkingTest(Path path) {
        if (workingTest != null) workingTest.CloseDocument();
        workingTest = new Test(path);
    }

    public static Path GetWorkingTest() {
        return workingTest.getPath();
    }
}

