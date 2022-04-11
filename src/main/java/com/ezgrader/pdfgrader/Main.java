package com.ezgrader.pdfgrader;


import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.io.InputStream;
import java.util.Locale;
import java.util.ResourceBundle;

public class Main extends Application {
    static {
        Font.loadFont(Main.class.getResource("/fa/fontawesome-webfont.ttf").toExternalForm(), 10);
    }

    @Override
    public void start(Stage primaryStage) throws Exception{

        // Font Awesome (Icons) -- Currently broken
        //ResourceBundle resources = ResourceBundle.getBundle("fa.fontawesome");


        GridPane root = FXMLLoader.load(getClass().getResource("grading.fxml"));

        // Allow vertical stretch
        ColumnConstraints col = new ColumnConstraints();
        col.setPercentWidth(100);
        root.getColumnConstraints().add(col);
        RowConstraints row = new RowConstraints();
        row.setPercentHeight(100);
        root.getRowConstraints().add(row);

        primaryStage.setTitle("PDF Grader");
        primaryStage.setScene(new Scene(root));
        primaryStage.sizeToScene();
        primaryStage.show();

        WebView pdfView = (WebView) root.lookup("#pdf-view");
        pdfView.getEngine().load("https://www.google.com");
    }


    public static void main(String[] args) {
        launch(args);
    }
}
