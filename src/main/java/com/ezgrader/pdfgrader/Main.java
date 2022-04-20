package com.ezgrader.pdfgrader;


import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.io.InputStream;
import java.util.Locale;
import java.util.ResourceBundle;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception{
        ResourceBundle resources = ResourceBundle.getBundle("fa.fontawesome");
        GridPane root = FXMLLoader.load(getClass().getResource("/com/ezgrader/pdfgrader/setup.fxml"), resources);
        MakeStretchy(root);

        primaryStage.setTitle("PDF Grader");
        primaryStage.setScene(new Scene(root));
        primaryStage.sizeToScene();
        primaryStage.show();
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
}
