package com.ezgrader.pdfgrader;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GradingController {
    @FXML
    private ImageView pdfView;
    @FXML
    private TextField pointsGiven;
    @FXML
    private VBox feedbacks;
    @FXML
    private TableView feedbackTable;
    @FXML
    private TableColumn pointsCol;
    @FXML
    private TextField feedbackNewPoints;
    @FXML
    private TextField feedbackNewDesc;
    @FXML
    private Pagination pagination;

    private ObservableList<Feedback> feedbackTest = FXCollections.observableArrayList();
    private Test test;

    @FXML
    public void initialize() {
        // Temp pdf
        //TODO: pass test data correctly so we don't have to do this part
        Path path = Paths.get(System.getProperty("user.dir") + "\\SPIF - PDF Grader.pdf");
        test = new Test(path);
        pagination.setPageCount(test.getTotalPages());
        pagination.setPageFactory(n -> new ImageView(test.renderPageImage(n)));

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

    public void setTest(Test test) {
        this.test = test;
    }

    /*
    @FXML
    private void handleTFAction(ActionEvent event) {
        TextField source = (TextField)event.getSource();
        System.out.println("You entered: "+source.getText());
    }
    */
}
