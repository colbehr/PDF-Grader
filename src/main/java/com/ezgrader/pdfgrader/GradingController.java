package com.ezgrader.pdfgrader;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;

public class GradingController {
    @FXML
    private VBox feedbacks;
    @FXML
    private TableView feedbackTable;
    @FXML
    private TextField feedbackNewPoints;
    @FXML
    private TextField feedbackNewDesc;

    private ObservableList<Feedback> feedbackTest = FXCollections.observableArrayList(

    );

    @FXML
    private void addFeedback() {
        if (feedbackNewDesc.getText().equals("")) return;

        feedbackTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        feedbackTest.add(new Feedback(feedbackNewPoints.getText(), feedbackNewDesc.getText()));
        feedbackTable.setItems(feedbackTest);

        feedbackNewPoints.setText("");
        feedbackNewDesc.setText("");
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
