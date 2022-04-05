package sample;

import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import javafx.scene.control.Button;
import sun.text.normalizer.VersionInfo;

public class Main extends Application {
    @Override
    public void init() throws Exception {
        super.init();
        //Font.loadFont(getClass().getResourceAsStream("../../fonts/Raleway-Regular.ttf"), 10);
    }

    @Override
    public void start(Stage primaryStage) throws Exception{

        GridPane root = FXMLLoader.load(getClass().getResource("grading.fxml"));
        // Vertical stretch works, not horizontal yet though
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
