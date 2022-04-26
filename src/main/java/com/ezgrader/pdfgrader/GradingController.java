package com.ezgrader.pdfgrader;


import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.text.Text;

import java.io.IOException;
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
    private Label questionNumberText;
    @FXML
    private Label questionsTotalText;
    @FXML
    private TextField pointsGivenField;
    @FXML
    private Label pointsTotalText;
    @FXML
    private CheckBox autoTotalCheckbox;
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
    @FXML
    private Button nextQuestionButton;
    @FXML
    private Button prevQuestionButton;
    @FXML
    private Button prevTestButton;
    @FXML
    private Button nextTestButton;

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
        prevQuestionButton.setDisable(true);

        currentTakenTest = 0;
        prevTestButton.setDisable(true);
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
    private void ToggleAutoTotal() {
        if (autoTotalCheckbox.isSelected()) {
            pointsGivenField.setEditable(false);
            autoTotal();
        } else {
            pointsGivenField.setEditable(true);
        }
    }

    private void autoTotal() {
        double total = 0;
        if (feedbacks != null && !feedbacks.isEmpty()) {
            String firstSign = feedbacks.get(0).getPoints().substring(0,1);
            total = firstSign.equals("+") ? 0 : workingTest.getQuestions().get(currentQuestion).getPointsPossible();
            for (Feedback feedback : feedbacks) {
                total += Double.parseDouble(feedback.getPoints().replace("+", ""));
            }
        }
        pointsGivenField.setText(total + "");
    }

    @FXML
    private void addFeedback() {
        if (feedbackNewDesc.getText().equals("")) return;

        feedbackTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        feedbacks.add(new Feedback(feedbackNewPoints.getText(), feedbackNewDesc.getText()));

        feedbackNewPoints.setText("");
        feedbackNewDesc.setText("");

        feedbackNewPoints.requestFocus();

        if (autoTotalCheckbox.isSelected()) autoTotal();
    }

    @FXML
    public void deleteFeedback(javafx.scene.input.KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.DELETE) {
            Feedback selectedItem = (Feedback) feedbackTable.getSelectionModel().getSelectedItem();
            feedbackTable.getItems().remove(selectedItem);

            if (autoTotalCheckbox.isSelected()) autoTotal();
        }
    }

    @FXML
    public void nextTest() {
        currentTakenTest = Math.min(currentTakenTest + 1, workingTest.getTakenTests().length - 1);
        loadCurrentTakenTest();
        if (currentTakenTest == workingTest.getTakenTests().length - 1) {
            nextTestButton.setDisable(true);
        }
        prevTestButton.setDisable(false);
    }

    @FXML
    public void prevTest() {
        currentTakenTest = Math.max(currentTakenTest - 1, 0);
        loadCurrentTakenTest();
        if (currentTakenTest == 0) {
            prevTestButton.setDisable(true);
        }
        nextTestButton.setDisable(false);
    }

    @FXML
    public void nextQuestion() {
        setCurrentQuestion(currentQuestion + 1);
        if (currentQuestion == workingTest.getQuestions().size() - 1) {
            nextQuestionButton.setDisable(true);
        }
        prevQuestionButton.setDisable(false);
    }

    @FXML
    public void prevQuestion() {
        setCurrentQuestion(currentQuestion - 1);
        if (currentQuestion == 0) {
            prevQuestionButton.setDisable(true);
        }
        nextQuestionButton.setDisable(false);
    }

    private void loadCurrentTakenTest() {
        TakenTest takenTest = workingTest.getTakenTests()[currentTakenTest];
        pointsGivenField.setText(takenTest.GetQuestionPointsGiven(currentQuestion) + "");
        feedbacks = takenTest.GetQuestionFeedbacks(currentQuestion);
        feedbackTable.setItems(feedbacks);
        currentTestText.setText(currentTakenTest + 1 + "");

        UpdatePagination();
    }

    private void setCurrentQuestion(int q) {
        if (workingTest != null && workingTest.getQuestions().size() >= q) {
            currentQuestion = q;
            questionNumberText.setText(q + 1 + "");

            // Update View
            pointsTotalText.setText(workingTest.getQuestions().get(currentQuestion).getPointsPossible() + "");
            loadCurrentTakenTest();
        }
    }

    private void UpdatePagination() {
        //set the current page number to the question's page number
        int questionPage = workingTest.getQuestions().get(currentQuestion).getPageNum() - 1;
        int currentTestOffset = currentTakenTest * workingTest.getPagesPerTest();
        int page = Math.min(questionPage + currentTestOffset, workingTest.getDocument().getNumberOfPages());
        pagination.setCurrentPageIndex(page);
    }

    public void finishedGrading(ActionEvent event) throws IOException {
        //finish grading and go to export page
        Main.SwitchScene("export.fxml");
    }
}
