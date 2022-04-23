package com.ezgrader.pdfgrader;


import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.text.Text;

import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.ezgrader.pdfgrader.Main.workingTest;

public class GradingController {
    @FXML
    private ImageView pdfView;
    @FXML
    private Label testNameText;
    @FXML
    private Text questionNumberText;
    @FXML
    private Text questionsTotalText;
    @FXML
    private TextField pointsGivenField;
    @FXML
    private Label pointsTotalText;
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
    @FXML
    private Text currentTestText;
    @FXML
    private Text totalTestsText;

    //private Test test;
    private int currentQuestion;
    private int currentTakenTest;
    private ObservableList<Feedback> feedbacks;

    @FXML
    public void initialize() {
        if (workingTest != null) {
            pagination.setPageCount(workingTest.getTotalPages());
            pagination.setPageFactory(n -> new ImageView(workingTest.renderPageImage(n)));
        }

        // INPUT SANITIZING
        pointsGivenField.setTextFormatter(TextFilters.GetDoubleFilter());

        // For feedback points, allow + or - followed by a decimal number
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
        // -------------

        // Grading setup
        setCurrentQuestion(0);
        currentTakenTest = 0;
        loadCurrentTakenTest();

        pointsGivenField.textProperty().addListener((observable, oldValue, newValue) -> {
            double points = Double.parseDouble(newValue);
            double maxPoints = workingTest.getQuestions().get(currentQuestion).getPointsPossible();
            if (points > maxPoints) {
                points = maxPoints;
                pointsGivenField.setText(points + "");
            }
            workingTest.getTakenTests()[currentTakenTest].GradeQuestion(currentQuestion, points);
        });

        // One-time view updates
        testNameText.setText(workingTest.getName());
        questionsTotalText.setText(workingTest.getQuestions().size() + "");
        totalTestsText.setText(workingTest.getTakenTests().length + "");
    }

    @FXML
    private void addFeedback() {
        if (feedbackNewDesc.getText().equals("")) return;

        feedbackTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        feedbacks.add(new Feedback(feedbackNewPoints.getText(), feedbackNewDesc.getText()));

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

    @FXML
    public void nextTest() {
        currentTakenTest = Math.min(currentTakenTest + 1, workingTest.getTakenTests().length - 1);
        loadCurrentTakenTest();
    }

    @FXML
    public void prevTest() {
        currentTakenTest = Math.max(currentTakenTest - 1, 0);
        loadCurrentTakenTest();
    }

    private void loadCurrentTakenTest() {
        TakenTest takenTest = workingTest.getTakenTests()[currentTakenTest];
        pointsGivenField.setText(takenTest.GetQuestionPointsGiven(currentQuestion) + "");
        feedbacks = takenTest.GetQuestionFeedbacks(currentQuestion);
        feedbackTable.setItems(feedbacks);

        currentTestText.setText(currentTakenTest + 1 + "");
    }

    private void setCurrentQuestion(int q) {
        if (workingTest != null && workingTest.getQuestions().size() >= q) {
            currentQuestion = q;
            pointsTotalText.setText(workingTest.getQuestions().get(currentQuestion).getPointsPossible() + "");
            loadCurrentTakenTest();
        }
    }

//    public void setTest(Test test) {
//        this.test = test;
//    }

    /*
    @FXML
    private void handleTFAction(ActionEvent event) {
        TextField source = (TextField)event.getSource();
        System.out.println("You entered: "+source.getText());
    }
    */
}
