package com.ezgrader.pdfgrader;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.web.WebView;

import java.util.function.UnaryOperator;
import java.util.regex.*;

public class GradingController {
    @FXML
    private WebView pdfView;
    @FXML
    private TextField pointsGiven;
    @FXML
    private VBox feedbacks;
    @FXML
    private TableView feedbackTable;
    @FXML
    private TextField feedbackNewPoints;
    @FXML
    private TextField feedbackNewDesc;

    private ObservableList<Feedback> feedbackTest = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Temp pdf view
        pdfView.getEngine().load("https://www.google.com");

        // Input sanitizing
        pointsGiven.setTextFormatter(TextFilters.GetDoubleFilter());

        UnaryOperator<TextFormatter.Change> filter = c -> {
            if (c.getText().equals("")) return c;
            String patternString = "([+-]?)((\\d+)\\.?(\\d)*)?";
            Pattern p = Pattern.compile(patternString);
            Matcher m = p.matcher(c.getControlNewText());
            if (!m.matches()) {
                c.setText("");
            } else if (((TextField)c.getControl()).getText().equals("")) {
                Pattern p2 = Pattern.compile("[0-9]");
                Matcher m2 = p2.matcher(c.getText());
                if (m2.matches()) {
                    c.setText("+" + c.getText());
                    int end = c.getControlNewText().length();
                    c.selectRange(end, end);
                }
            }
            return c;
        };
        feedbackNewPoints.setTextFormatter(new TextFormatter<>(filter));
    }

    @FXML
    private void addFeedback() {
        if (feedbackNewDesc.getText().equals("")) return;

        feedbackTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        feedbackTest.add(new Feedback(feedbackNewPoints.getText(), feedbackNewDesc.getText()));
        feedbackTable.setItems(feedbackTest);

        feedbackNewPoints.setText("");
        feedbackNewDesc.setText("");

        feedbackNewPoints.requestFocus();
    }

    @FXML
    public void deleteFeedback(javafx.scene.input.KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.DELETE) {
            Feedback selectedItem = (Feedback) feedbackTable.getSelectionModel().getSelectedItem();
            feedbackTable.getItems().remove(selectedItem);
        }
    }

    /*
    @FXML
    private void handleTFAction(ActionEvent event) {
        TextField source = (TextField)event.getSource();
        System.out.println("You entered: "+source.getText());
    }
    */
}
