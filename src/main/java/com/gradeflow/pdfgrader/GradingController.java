package com.gradeflow.pdfgrader;


import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.util.Callback;
import javafx.util.converter.DefaultStringConverter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.gradeflow.pdfgrader.PDFGrader.getStage;
import static com.gradeflow.pdfgrader.PDFGrader.workingTest;

public class GradingController {
    @FXML
    public Text gradingSaveMenuText;
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
    private TableColumn explanationCol;
    @FXML
    private TextField feedbackNewPoints;
    @FXML
    private TextField feedbackNewDesc;
    @FXML
    private TableView reuseFeedbackTable;
    @FXML
    private ZoomPanPagination pagination;
    @FXML
    private Label currentTestText;
    @FXML
    private Label totalTestsText;
    @FXML
    private Button nextQuestionButton;
    @FXML
    private Button prevQuestionButton;
    @FXML
    private Button prevTestButton;
    @FXML
    private Button nextTestButton;
    @FXML
    private TextField testOwnerField;

    private int currentQuestion;
    private int currentTakenTest;
    private ObservableList<Feedback> feedbacks;
    private Double zoomLevel = 1.0;
    private Double zoomSensitivity = 0.005;
    private Double lastDragX = 0.0;
    private Double lastDragY = 0.0;
    private Double panX = 0.0;
    private Double panY = 0.0;
    private List<ImageView> pageImages = new ArrayList<>();

    @FXML
    public void initialize() {
        // INPUT SANITIZING
        pointsGivenField.setTextFormatter(TextFilters.GetDoubleFilter());

        // For feedback points, allow + or - followed by a decimal number
        UnaryOperator<TextFormatter.Change> filter = c -> {
            if (c.getText().equals("")) {
                return c;
            }
            String patternString = TextFilters.pointsRegex;
            Pattern p = Pattern.compile(patternString);
            Matcher m = p.matcher(c.getControlNewText());
            if (!m.matches()) {
                c.setText("");
            } else if (((TextField) c.getControl()).getText().equals("")) {
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

        testOwnerField.setTextFormatter(TextFilters.MakeFilter("[\\w-]*"));
        testOwnerField.focusedProperty().addListener((obs, oldVal, newVal) -> {
                if (!newVal) updateTakenTestId(null);
        });

        // Grading setup
        int[] savedPlace = workingTest.getSavedPlace();
        setCurrentQuestion(savedPlace[0]);
        setCurrentTakenTest(savedPlace[1]);

        pointsGivenField.textProperty().addListener((observable, oldValue, newValue) -> {
            double points = Double.parseDouble(newValue);
            // Taking point cap functionality out for now because of user case where extra
            // credit may be given on a question.
//            double maxPoints = workingTest.getQuestions().get(currentQuestion).getPointsPossible();
//            if (points > maxPoints) {
//                points = maxPoints;
//                pointsGivenField.setText(points + "");
//            }
            workingTest.getTakenTests()[currentTakenTest].GradeQuestion(currentQuestion, points);
        });

        autoTotalCheckbox.setSelected(true);
        ToggleAutoTotal();

        // One-time view updates
        testNameText.setText(workingTest.getName());
        questionsTotalText.setText(workingTest.getQuestions().size() + "");
        totalTestsText.setText(workingTest.getTakenTests().length + "");
        addButtonToReuseFeedbacksTable();
        setTableEditable();
        Platform.runLater(() -> {
            getStage().setTitle("Gradeflow - " + workingTest.getName());
            getStage().getIcons().add(new Image(getClass().getResourceAsStream("img/journals-square.png")));
        }); // reset title
        getUsedFeedbacks();
        Platform.runLater(this::setupKeyboardShortcuts);

        //https://stackoverflow.com/questions/6864540/how-to-set-a-javafx-stage-frame-to-maximized
        getStage().setMaximized(true);
        ObservableList<Screen> screens = Screen.getScreensForRectangle(new Rectangle2D(getStage().getX(), getStage().getY(), getStage().getWidth(), getStage().getHeight()));

        // Change stage properties
        Rectangle2D bounds = screens.get(0).getVisualBounds();
        getStage().setX(bounds.getMinX());
        getStage().setY(bounds.getMinY());
        getStage().setWidth(bounds.getWidth());
        getStage().setHeight(bounds.getHeight());
    }

    /**
     * Method called by Auto Total checkbox to toggle score
     * being summed from Feedbacks or entered manually
     */
    @FXML
    private void ToggleAutoTotal() {
        if (autoTotalCheckbox.isSelected()) {
            pointsGivenField.setEditable(false);
            autoTotal();
        } else {
            pointsGivenField.setEditable(true);
        }
    }

    /**
     * Totals question score from Feedback
     */
    private void autoTotal() {
        double total = 0;
        if (feedbacks != null && !feedbacks.isEmpty()) {
            String firstSign = feedbacks.get(0).getPoints().length() > 0 ?
                    feedbacks.get(0).getPoints().substring(0, 1) : "+";
            // If the first feedback is subtractive (rather than additive), begin from full points
            total = firstSign.equals("-") ? workingTest.getQuestions().get(currentQuestion).getPointsPossible() : 0;
            for (Feedback feedback : feedbacks) {
                if (feedback.getPoints().length() > 0) {
                    total += Double.parseDouble(feedback.getPoints().replace("+", ""));
                }
            }
        }
        pointsGivenField.setText(total + "");
    }

    /**
     * Adds a blank new feedback for the TakenTest
     */
    @FXML
    private void addFeedback() {
        if (feedbackNewDesc.getText().equals("")) {
            return;
        }

        addFeedback(new Feedback(feedbackNewPoints.getText(), feedbackNewDesc.getText()));
    }

    /**
     * Copys an existing feedback into this TakenTest's Feedbacks for this question.
     * @param f feedback to add
     */
    private void addFeedback(Feedback f) {
        feedbackTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        feedbacks.add(f.copy());

        feedbackNewPoints.setText("");
        feedbackNewDesc.setText("");

        feedbackNewPoints.requestFocus();

        if (autoTotalCheckbox.isSelected()) {
            autoTotal();
        }
    }

    /**
     * Called on cell key press. Deletes currently selected feedback if key was DELETE
     * @param keyEvent
     */
    @FXML
    public void deleteFeedback(javafx.scene.input.KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.DELETE) {
            Feedback selectedItem = (Feedback) feedbackTable.getSelectionModel().getSelectedItem();
            feedbackTable.getItems().remove(selectedItem);

            if (autoTotalCheckbox.isSelected()) {
                autoTotal();
            }
        }
    }

    /**
     * Updates UI to show next test
     */
    @FXML
    public void nextTest() {
        setCurrentTakenTest(currentTakenTest + 1);
    }

    /**
     * Updates UI to show previous test
     */
    @FXML
    public void prevTest() {
        setCurrentTakenTest(currentTakenTest - 1);
    }

    /**
     * Updates UI to show next question
     */
    @FXML
    public void nextQuestion() {
        setCurrentQuestion(currentQuestion + 1);
    }

    /**
     * Updates UI to show previous question
     */
    @FXML
    public void prevQuestion() {
        setCurrentQuestion(currentQuestion - 1);
    }

    /**
     * Calls SaveLoad to save test. User chooses a save location if Test has not ever been saved.
     */
    @FXML
    public void SaveTest() {
        if (workingTest.savePath == null) {
            SaveTestAs();
        } else {
            SaveLoad.SaveTest(workingTest, workingTest.savePath.toString(), currentQuestion, currentTakenTest);
            Toast.Notification("Saved " + workingTest.savePath.getFileName());
        }
    }

    /**
     * User chooses a folder to save test in. Save location is stored for later saves.
     */
    @FXML
    public void SaveTestAs() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("JSON Files", "*.json", "*.JSON"));
        //get parent folder of original pdf
        File f = new File(workingTest.getPdfPath().toFile().getAbsoluteFile().toString());
        String path = f.getParentFile().getAbsolutePath();
        String currentPath = Paths.get(path).toAbsolutePath().normalize().toString();
        //set initial folder to current pdf directory
        fileChooser.setInitialDirectory(new File(currentPath));
        fileChooser.setInitialFileName(workingTest.getName() + ".json");
        File outFile = fileChooser.showSaveDialog(PDFGrader.getStage().getScene().getWindow());
        if (outFile != null) {
            SaveLoad.SaveTest(workingTest, outFile.getPath(), currentQuestion, currentTakenTest);
            workingTest.savePath = outFile.toPath();
            Toast.Notification("Saved " + outFile.getName());
        }
    }

    /**
     * Saves test in same location as the Test's PDF.
     */
    @FXML
    public void SaveTestAsDefault() {
        //get parent folder of original pdf
        File f = new File(workingTest.getPdfPath().toFile().getAbsoluteFile().toString());
        String path = removeExtention(f.getAbsolutePath()) + ".json";
        System.out.println("Saved: " + path);
        String currentPath = Paths.get(path).toAbsolutePath().normalize().toString() + "";
        //set initial folder to current pdf directory
        SaveLoad.SaveTest(workingTest, currentPath, currentQuestion, currentTakenTest);
    }

    /**
     * Removes the extension from a filepath
     * @param filePath
     * @return
     */
    // https://stackoverflow.com/questions/3449218/remove-filename-extension-in-java
    public static String removeExtention(String filePath) {
        // These first few lines the same as Justin's
        File f = new File(filePath);

        // if it's a directory, don't remove the extention
        if (f.isDirectory()) {
            return filePath;
        }

        String name = f.getName();

        // Now we know it's a file - don't need to do any special hidden
        // checking or contains() checking because of:
        final int lastPeriodPos = name.lastIndexOf('.');
        if (lastPeriodPos <= 0) {
            // No period after first character - return name as it was passed in
            return filePath;
        } else {
            // Remove the last period and everything after it
            File renamed = new File(f.getParent(), name.substring(0, lastPeriodPos));
            return renamed.getPath();
        }
    }

    /**
     * Updates UI to display the TakenTest with the given index at the current question.
     * Enables/Disables relevant buttons if at the start/end index.
     * @param t
     */
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

    /**
     * Updates relevant UI fields with the current TakenTest data for this question
     */
    private void loadCurrentTakenTest() {
        TakenTest takenTest = workingTest.getTakenTests()[currentTakenTest];
        pointsGivenField.setText(takenTest.GetQuestionPointsGiven(currentQuestion) + "");
        feedbacks = takenTest.GetQuestionFeedbacks(currentQuestion);
        feedbackTable.setItems(feedbacks);
        currentTestText.setText(currentTakenTest + 1 + "");
        testOwnerField.setText(takenTest.getId());

        getUsedFeedbacks();
        UpdatePagination();
        Platform.runLater(() -> feedbackNewPoints.requestFocus());
    }

    /**
     * Updates the UI to display this question with the first TakenTest
     * @param q
     */
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

    /**
     * Gets the feedbacks used for this question on other TakenTests and fills the related UI table
     */
    private void getUsedFeedbacks() {
        ObservableList<Feedback> usedFeedbacks = FXCollections.observableArrayList();
        Set<String> usedFeedbackExplanations = new HashSet<>();

        //insert dummy feedbacks for quick grading
        usedFeedbacks.add(new Feedback("+" + workingTest.getTakenTests()[0].getTest().getQuestions().get(currentQuestion).getPointsPossible(), "Good Job"));
        usedFeedbacks.add(new Feedback("+" + workingTest.getTakenTests()[0].getTest().getQuestions().get(currentQuestion).getPointsPossible()/2, "Needs work"));
        usedFeedbacks.add(new Feedback("+" + 0, "No points"));
        for (Feedback f : usedFeedbacks) {
            usedFeedbackExplanations.add(f.getPoints() + f.getExplanation());
        }

        // Get feedbacks used by other test takers
        for (TakenTest t : workingTest.getTakenTests()) {
            for (Feedback f : t.GetQuestionFeedbacks(currentQuestion)) {

                //add regular feedback for loading
                if (!usedFeedbackExplanations.contains(f.getPoints() + f.getExplanation())) {
                    usedFeedbacks.add(f);
                }
                usedFeedbackExplanations.add(f.getPoints() + f.getExplanation());
            }
        }

        reuseFeedbackTable.setItems(usedFeedbacks);
        reuseFeedbackTable.refresh();
    }

    /**
     * Displays the current proper page in the test PDF
     */
    private void UpdatePagination() {
        //set the current page number to the question's page number
        int questionPage = workingTest.getQuestions().get(currentQuestion).getPageNum() - 1;
        int currentTestOffset = currentTakenTest * workingTest.getPagesPerTest();
        int page = Math.min(questionPage + currentTestOffset, workingTest.getTotalPages());
        pagination.setCurrentPageIndex(page);
    }

    /**
     * Updates the current TakenTest's id when the user changes it through the UI
     * @param e
     */
    @FXML
    private void updateTakenTestId(ActionEvent e) {
        if (testOwnerField.getText().length() > 0) {
            workingTest.getTakenTests()[currentTakenTest].setId(testOwnerField.getText());
        } else {
            testOwnerField.setText(workingTest.getTakenTests()[currentTakenTest].getId());
        }
    }

    /**
     * Adds buttons to all Feedbacks in the reuse feedbacks table so they may be added to the
     * current TakenTest.
     */
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


    /**
     * Initializes the feedback table to be editable
     */
    private void setTableEditable() {
        feedbackTable.setEditable(true);

        feedbackTable.setOnKeyPressed(event -> {
            if (event.getCode().isDigitKey()) {
                editFocusedCell();
            } else if (event.getCode() == KeyCode.DELETE) {
                deleteFeedback(event);
            }
        });
        // Update total after edits
        feedbackTable.editingCellProperty().addListener((observableValue, o, t1) -> autoTotal());

        // START EDIT EVENT
        feedbackTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && (feedbackTable.getFocusModel().getFocusedIndex() > -1) ) {
                TablePosition selectedCellPosition = feedbackTable.getFocusModel().getFocusedCell();
                Platform.runLater(() -> feedbackTable.edit(selectedCellPosition.getRow(), selectedCellPosition.getTableColumn()));
                autoTotal();
            }
        });

        // use custom editable cells
        pointsCol.setCellFactory(EditableTableCell.<Question, String>forTableColumn(new DefaultStringConverter(), TextFilters.pointsRegex));
        explanationCol.setCellFactory(EditableTableCell.<Question, String>forTableColumn(new DefaultStringConverter(), TextFilters.anyRegex));

        // No sorting columns except Question Number
        pointsCol.setSortable(false);
        explanationCol.setSortable(false);
    }

    @SuppressWarnings("unchecked")
    private void editFocusedCell() {
        final TablePosition<Question, ?> focusedCell = (TablePosition<Question, ?>) ((TableView.TableViewFocusModel)feedbackTable
                .focusModelProperty().get()).focusedCellProperty().get();
        feedbackTable.edit(focusedCell.getRow(), focusedCell.getTableColumn());
    }

    /**
     * Initializes keyboard shortcuts for this UI grading page.
     */
    private void setupKeyboardShortcuts() {
        UpdateShortcutMenuText();

        getStage().getScene().addEventFilter(KeyEvent.KEY_PRESSED, ke -> {
            if (Shortcuts.get("gradingSave").match(ke)) {
                SaveTest();
                ke.consume();
            } else if (Shortcuts.get("gradingNextTest").match(ke)) {
                nextTest();
                ke.consume(); // <-- stops passing the event to next node
            } else if (Shortcuts.get("gradingPrevTest").match(ke)) {
                prevTest();
                ke.consume();
            } else if (Shortcuts.get("gradingNextQuestion").match(ke)) {
                nextQuestion();
                ke.consume();
            } else if (Shortcuts.get("gradingPrevQuestion").match(ke)) {
                prevQuestion();
                ke.consume();
            } else if (Shortcuts.get("gradingGotoPoints").match(ke)) {
                feedbackNewPoints.requestFocus();
                ke.consume();
            } else if (Shortcuts.get("gradingAddFeedback").match(ke)) {
                addFeedback();
                ke.consume();
            } else if (Shortcuts.get("prevPage").match(ke)) {
                int index = pagination.getCurrentPageIndex() - 1;
                pagination.setCurrentPageIndex(Math.max(index, 0));
            } else if (Shortcuts.get("nextPage").match(ke)) {
                int index = pagination.getCurrentPageIndex() + 1;
                pagination.setCurrentPageIndex(Math.min(index, workingTest.getTotalPages() - 1));
            } else {
                // REUSE FEEDBACKS
                for (int i = 1; i <= 9; i++) {
                    if (ke.isControlDown() && ke.getCode() == KeyCode.getKeyCode("" + i)) {
                        if (i-1 < reuseFeedbackTable.getItems().size()) {
                            Feedback f = (Feedback) reuseFeedbackTable.getItems().get(i - 1);
                            if (!feedbacks.contains(f)) {
                                addFeedback(f);
                            }
                        }
                    }
                }
            }
        });
    }

    /**
     * Updates the shortcuts shown in menu items upon changing shortcut keybinds
     */
    private void UpdateShortcutMenuText() {
        gradingSaveMenuText.setText(Shortcuts.get("gradingSave").getName());
    }

    /**
     * Opens dialog with the relevant shortcuts for this UI grading page.
     */
    @FXML
    private void ShowShortcutDialog() {
        String[] keywords = {"grading", "page"};
        Shortcuts.ShowShortcutDialog(keywords, "Grading Shortcuts");
    }

    // Utility functions that are visible to FXML file
    @FXML
    public void OpenTest() throws IOException { PDFGrader.OpenTest(); }
    @FXML
    public void GoToSetup() throws IOException { PDFGrader.GoToSetup(); }
    @FXML
    private void Exit() { PDFGrader.Exit(); }
    @FXML
    private void ExitToHome() throws IOException {
        SaveTest();
        PDFGrader.SwitchScene("home.fxml");
    }
    @FXML
    private void OpenGithub() { PDFGrader.OpenGithub(); }
    @FXML
    private void OpenAbout() { PDFGrader.showAboutPage(); }
    @FXML
    public void Export() { Export.simpleExport(); }
    @FXML
    public void SetExportFolder() { Export.browseAndSetExportFolder(); }
}
