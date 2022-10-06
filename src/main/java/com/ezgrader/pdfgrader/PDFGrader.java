package com.ezgrader.pdfgrader;


import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

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
        Font.loadFont(PDFGrader.class.getResource("/fa/fontawesome-webfont.ttf").toExternalForm(), 10); // Icon Support
        launch(args);
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
        GridPane newRoot = FXMLLoader.load(PDFGrader.class.getResource(sceneName));
        MakeStretchy(newRoot);

        if (maintainSize || stage.isMaximized()) {
            // force maintaining window size
            newRoot.setMinSize(stage.getWidth(), stage.getHeight());
            newRoot.setMaxSize(stage.getWidth(), stage.getHeight());
        }
        stage.setScene(new Scene(newRoot));
        // set back
        newRoot.setMinSize(MIN_WIDTH, MIN_HEIGHT);
        newRoot.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
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
        File file = fileChooser.showOpenDialog(PDFGrader.getStage().getScene().getWindow());
        return file;
    }

    public static void SetWorkingTest(Path path) {
        if (workingTest != null) workingTest.CloseDocument();
        workingTest = new Test(path);
    }
}
