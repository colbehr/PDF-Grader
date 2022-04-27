package com.ezgrader.pdfgrader;


import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ResourceBundle;

public class Main extends Application {

    public static Test workingTest;
    private static Stage stage;

    @Override
    public void start(Stage primaryStage) throws Exception{
        ResourceBundle resources = ResourceBundle.getBundle("fa.fontawesome");
        GridPane root = FXMLLoader.load(getClass().getResource("/com/ezgrader/pdfgrader/home.fxml"), resources);
        MakeStretchy(root);

        primaryStage.setTitle("PDF Grader");
        primaryStage.setScene(new Scene(root));
        primaryStage.sizeToScene();
        primaryStage.show();
        stage = primaryStage;
    }

    public static void main(String[] args) {
        Font.loadFont(Main.class.getResource("/fa/fontawesome-webfont.ttf").toExternalForm(), 10); // Icon Support
        launch(args);
    }

    public static void MakeStretchy(GridPane root) {
        // Allow vertical stretch
        ColumnConstraints col = new ColumnConstraints();
        col.setPercentWidth(100);
        root.getColumnConstraints().add(col);
        RowConstraints row = new RowConstraints();
        row.setPercentHeight(100);
        root.getRowConstraints().add(row);
    }

    public static void SwitchScene(String sceneName) throws IOException {
        GridPane newRoot = FXMLLoader.load(Main.class.getResource(sceneName));
        MakeStretchy(newRoot);
        stage.setScene(new Scene(newRoot));
    }

}
