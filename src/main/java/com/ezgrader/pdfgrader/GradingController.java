package com.ezgrader.pdfgrader;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.util.Callback;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.ezgrader.pdfgrader.PDFGrader.workingTest;

public class GradingController {
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
    private TableView reuseFeedbackTable;
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
        int savedPlace[] = workingTest.getSavedPlace();
        setCurrentQuestion(savedPlace[0]);
        setCurrentTakenTest(savedPlace[1]);

        pointsGivenField.textProperty().addListener((observable, oldValue, newValue) -> {
            double points = Double.parseDouble(newValue);
            double maxPoints = workingTest.getQuestions().get(currentQuestion).getPointsPossible();
            if (points > maxPoints) {
                points = maxPoints;
                pointsGivenField.setText(points + "");
            }

            workingTest.getTakenTests()[currentTakenTest].GradeQuestion(currentQuestion, points);
        });

        autoTotalCheckbox.setSelected(true);
        ToggleAutoTotal();

        // One-time view updates
        testNameText.setText(workingTest.getName());
        questionsTotalText.setText(workingTest.getQuestions().size() + "");
        totalTestsText.setText(workingTest.getTakenTests().length + "");
        addButtonToReuseFeedbacksTable();
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
            String firstSign = feedbacks.get(0).getPoints().length() > 0 ?
                    feedbacks.get(0).getPoints().substring(0,1) : "+";
            // If the first feedback is subtractive (rather than additive), begin from full points
            total = firstSign.equals("+") ? 0 : workingTest.getQuestions().get(currentQuestion).getPointsPossible();
            for (Feedback feedback : feedbacks) {
                if (feedback.getPoints().length() > 0) {
                    total += Double.parseDouble(feedback.getPoints().replace("+", ""));
                }
            }
        }
        pointsGivenField.setText(total + "");
    }

    @FXML
    private void addFeedback() {
        if (feedbackNewDesc.getText().equals("")) return;

        addFeedback(new Feedback(feedbackNewPoints.getText(), feedbackNewDesc.getText()));
    }

    private void addFeedback(Feedback f) {
        feedbackTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        feedbacks.add(f);

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
        setCurrentTakenTest(currentTakenTest + 1);
    }

    @FXML
    public void prevTest() {
        setCurrentTakenTest(currentTakenTest - 1);
    }

    @FXML
    public void nextQuestion() {
        setCurrentQuestion(currentQuestion + 1);
    }

    @FXML
    public void prevQuestion() {
        setCurrentQuestion(currentQuestion - 1);
    }

    @FXML
    public void SaveTest() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json", "*.JSON"));
        File outFile = fileChooser.showSaveDialog(PDFGrader.getStage().getScene().getWindow());
        if (outFile != null) {
            SaveLoad.SaveTest(workingTest, outFile.getPath(), currentQuestion, currentTakenTest);
        }
    }

    private void setCurrentTakenTest(int t) {
        // safe clamped assignment
        currentTakenTest = Math.min(Math.max(0, t), workingTest.getTakenTests().length - 1);
        loadCurrentTakenTest();
        // Ensure safe buttons, including case of only 1 taken test
        if (currentTakenTest == 0) {
            nextTestButton.setDisable(false);
            prevTestButton.setDisable(true);
        } else {
            nextTestButton.setDisable(false);
            prevTestButton.setDisable(false);
        }
        if (currentTakenTest == workingTest.getTakenTests().length - 1) {
            nextTestButton.setDisable(true);
        }
    }

    private void loadCurrentTakenTest() {
        TakenTest takenTest = workingTest.getTakenTests()[currentTakenTest];
        pointsGivenField.setText(takenTest.GetQuestionPointsGiven(currentQuestion) + "");
        feedbacks = takenTest.GetQuestionFeedbacks(currentQuestion);
        feedbackTable.setItems(feedbacks);
        currentTestText.setText(currentTakenTest + 1 + "");

        getUsedFeedbacks();

        UpdatePagination();
    }

    private void setCurrentQuestion(int q) {
        if (workingTest != null) {
            // safe clamped assignment
            currentQuestion = Math.min(Math.max(0, q), workingTest.getQuestions().size() - 1);
            questionNumberText.setText(currentQuestion + 1 + "");

            setCurrentTakenTest(0);

            // Update View
            pointsTotalText.setText(workingTest.getQuestions().get(currentQuestion).getPointsPossible() + "");
            // Ensure safe buttons, including case of only 1 question
            if (currentQuestion == 0) {
                nextQuestionButton.setDisable(false);
                prevQuestionButton.setDisable(true);
            } else {
                nextQuestionButton.setDisable(false);
                prevQuestionButton.setDisable(false);
            }
            if (currentQuestion == workingTest.getQuestions().size() - 1) {
                nextQuestionButton.setDisable(true);
            }
        }
    }

    private void getUsedFeedbacks() {
        ObservableList<Feedback> usedFeedbacks = FXCollections.observableArrayList();
        Set<String> usedFeedbackExplanations = new HashSet<>();
        for (TakenTest t: workingTest.getTakenTests()) {
            for (Feedback f : t.GetQuestionFeedbacks(currentQuestion)) {
                System.out.println(f.getExplanation());
                if (!usedFeedbackExplanations.contains(f.getExplanation())) {
                    usedFeedbacks.add(f);
                }
                usedFeedbackExplanations.add(f.getExplanation());
            }
        }
        reuseFeedbackTable.setItems(usedFeedbacks);
    }

    private void UpdatePagination() {
        //set the current page number to the question's page number
        int questionPage = workingTest.getQuestions().get(currentQuestion).getPageNum() - 1;
        int currentTestOffset = currentTakenTest * workingTest.getPagesPerTest();
        int page = Math.min(questionPage + currentTestOffset, workingTest.getTotalPages());
        pagination.setCurrentPageIndex(page);
    }

    public void finishedGrading(ActionEvent event) throws IOException {
        //finish grading and go to export page
        PDFGrader.SwitchScene("export.fxml");
    }

    // Thanks to https://riptutorial.com/javafx/example/27946/add-button-to-tableview
    private void addButtonToReuseFeedbacksTable() {
        TableColumn<Feedback, Void> colBtn = new TableColumn("Add");

        Callback<TableColumn<Feedback, Void>, TableCell<Feedback, Void>> cellFactory = new Callback<TableColumn<Feedback, Void>, TableCell<Feedback, Void>>() {
            @Override
            public TableCell<Feedback, Void> call(final TableColumn<Feedback, Void> param) {
                final TableCell<Feedback, Void> cell = new TableCell<Feedback, Void>() {

                    private final Button btn = new Button("+");

                    {
                        btn.getStyleClass().add("small-button");
                        btn.setOnAction((ActionEvent event) -> {
                            Feedback f = getTableView().getItems().get(getIndex());
                            if (!feedbacks.contains(f)) {
                                addFeedback(f);
                            }
                        });
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(btn);
                        }
                    }
                };
                return cell;
            }
        };
        colBtn.setCellFactory(cellFactory);
        reuseFeedbackTable.getColumns().add(colBtn);

    }
}
