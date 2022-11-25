package com.gradeflow.pdfgrader;


import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.util.Callback;
import javafx.util.converter.DefaultStringConverter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.awt.*;
import java.awt.image.BufferedImage;
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
    public static Path folderPath;

    @FXML
    public static String folderPathText;

    public static Path statisticsPath;

    public static String filePathText;
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
        int savedPlace[] = workingTest.getSavedPlace();
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

    @FXML
    private void addFeedback() {
        if (feedbackNewDesc.getText().equals("")) {
            return;
        }

        addFeedback(new Feedback(feedbackNewPoints.getText(), feedbackNewDesc.getText()));
    }

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
        if (workingTest.savePath == null) {
            SaveTestAs();
        } else {
            SaveLoad.SaveTest(workingTest, workingTest.savePath.toString(), currentQuestion, currentTakenTest);
            Toast.Notification("Saved " + workingTest.savePath.getFileName());
        }
    }

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
        testOwnerField.setText(takenTest.getId());

        getUsedFeedbacks();
        UpdatePagination();
        Platform.runLater(() -> feedbackNewPoints.requestFocus());
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

    private void UpdatePagination() {
        //set the current page number to the question's page number
        int questionPage = workingTest.getQuestions().get(currentQuestion).getPageNum() - 1;
        int currentTestOffset = currentTakenTest * workingTest.getPagesPerTest();
        int page = Math.min(questionPage + currentTestOffset, workingTest.getTotalPages());
        pagination.setCurrentPageIndex(page);
    }

    @FXML
    private void updateTakenTestId(ActionEvent e) {
        if (testOwnerField.getText().length() > 0) {
            workingTest.getTakenTests()[currentTakenTest].setId(testOwnerField.getText());
        } else {
            testOwnerField.setText(workingTest.getTakenTests()[currentTakenTest].getId());
        }
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

    private void UpdateShortcutMenuText() {
        gradingSaveMenuText.setText(Shortcuts.get("gradingSave").getName());
    }

    @FXML
    private void ShowShortcutDialog() {
        String[] keywords = {"grading", "page"};
        Shortcuts.ShowShortcutDialog(keywords, "Grading Shortcuts");
    }

    //beginning of the insertion of exportController


    public void browseForTestFolder(ActionEvent event) {
        //open a FileChooser when ChoosePDF is clicked
        DirectoryChooser folderChooser = new DirectoryChooser();
        folderChooser.setTitle("Choose a folder to save graded files");
        //Set initial directory to users Desktop
        folderChooser.setInitialDirectory(new File(System.getProperty("user.home") + System.getProperty("file.separator") + "Desktop"));
        File pdf = folderChooser.showDialog(getStage());
        if (pdf != null) {
            folderPath = Paths.get(pdf.getPath());
            folderPathText = folderPath.toString();
        }
    }

    @FXML
    private void browseForStatistics(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter pdfFilter = new FileChooser.ExtensionFilter("PDF files (*.pdf)", "*.pdf", "*.PDF");
        fileChooser.getExtensionFilters().add(pdfFilter);
        fileChooser.setTitle("Choose a location to save statistics overview");
        //Set initial directory to users Desktop
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home") + System.getProperty("file.separator") + "Desktop"));
        if (workingTest != null) {
            fileChooser.setInitialFileName(workingTest.getName() + "_statistics.pdf");
        } else {
            fileChooser.setInitialFileName("grade_statistics.pdf");
        }
        File pdf = fileChooser.showSaveDialog(getStage());
        if (pdf != null) {
            statisticsPath = Paths.get(pdf.getPath());
            filePathText = statisticsPath.toString();
        }
    }

    @FXML
    private void exportFiles(ActionEvent event) throws IOException {
        if (statisticsPath == null || folderPath == null) {
            String errorMessage = "";
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Path has not been set");
            if (statisticsPath == null) {
                errorMessage = "Please find a location for the statistics file.\n\n";
            }
            if (folderPath == null) {
                errorMessage += "Please find a folder for graded files.";
            }
            alert.setContentText(errorMessage);
            alert.showAndWait();
            return;
        }
        exportStats();
        System.out.println("Exported statistics");

        //TODO: catch error when the tests cant be fully exported because of file replacement issue, show error on screen
        exportTests();
        System.out.println("Exported students tests");

        //open dialog, return to home
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Finished Exporting");
        alert.setHeaderText("Files exported.");
        alert.setContentText("Statistics exported to \n" + statisticsPath.toString() + "\n\nFiles exported to \n" + folderPath.toString());
        alert.showAndWait();

        try {
            PDFGrader.SwitchScene("home.fxml");
        } catch (IOException e) {
            System.exit(0);
        }
    }

    private void exportStats() throws IOException {
        PDDocument statsDoc = new PDDocument();
        statsDoc.addPage(new PDPage());
        int curPage = 0;
        PDPage thisPage = statsDoc.getPage(curPage);
        curPage++;

        int pageHeight = (int) thisPage.getTrimBox().getHeight();
        int pageWidth = (int) thisPage.getTrimBox().getWidth();

        PDPageContentStream contentStream = new PDPageContentStream(statsDoc, thisPage);

        //creating table
        contentStream.setStrokingColor(Color.DARK_GRAY);
        contentStream.setLineWidth(1);

        int initX = 50;
        int initY = pageHeight - 50;
        int cellHeight = 30;
        int cellWidth = 100;
        int tableCounter = 1;
        double pointArray[];
        pointArray = new double[workingTest.getTakenTests().length];
        int mean = 0;
        double median;

        int colCount = workingTest.getQuestions().size() + 2;
        int rowCount = workingTest.getTakenTests().length;

        contentStream.beginText();
        contentStream.newLineAtOffset(initX + 18, initY - cellHeight + 10);
        contentStream.setFont(PDType1Font.TIMES_ROMAN, 18);
        contentStream.showText("Student");
        contentStream.endText();

        contentStream.addRect(initX, initY, cellWidth, -cellHeight);
        initX += cellWidth;

        contentStream.addRect(initX, initY, cellWidth, -cellHeight);

        contentStream.beginText();
        contentStream.newLineAtOffset(initX + 10, initY - cellHeight + 10);
        contentStream.setFont(PDType1Font.TIMES_ROMAN, 18);
        contentStream.showText("Total Score");
        contentStream.endText();

        initX = 50;
        initY -= cellHeight;

        for (int i = 1; i <= rowCount; i++) {
            if (tableCounter == 23) {
                contentStream.stroke();
                contentStream.close();
                tableCounter = 1;
                initX = 50;
                initY = pageHeight-50;
                statsDoc.addPage(new PDPage());
                thisPage = statsDoc.getPage(curPage);
                curPage++;
                contentStream = new PDPageContentStream(statsDoc, thisPage);
            }
            contentStream.beginText();
            contentStream.newLineAtOffset(initX + 10, initY - cellHeight + 10);
            contentStream.setFont(PDType1Font.TIMES_ROMAN, 18);
            //TODO: Figure out a way to label the students correctly
            contentStream.showText("Student " + i);
            contentStream.endText();

            for (int j = 1; j <= 2; j++) {
                contentStream.addRect(initX, initY, cellWidth, -cellHeight);
                initX += cellWidth;
                if (j == 2) {
                    double totalPoints = workingTest.getTakenTests()[i - 1].GetTotalPoints();
                    pointArray[i-1] = workingTest.getTakenTests()[i - 1].GetTotalPoints();
                    mean += totalPoints;
                    contentStream.beginText();
                    contentStream.newLineAtOffset(initX + 10 - cellWidth, initY - cellHeight + 10);
                    contentStream.setFont(PDType1Font.TIMES_ROMAN, 18);
                    contentStream.showText(String.valueOf(totalPoints));
                    contentStream.endText();
                }
            }
            initX = 50;
            initY -= cellHeight;
            tableCounter++;
        }

        //TODO: seperate table for other stats (mean, median, more???)
        contentStream.stroke();
        contentStream.close();
        statsDoc.addPage(new PDPage());
        PDPage overviewPage = statsDoc.getPage(curPage);
        contentStream = new PDPageContentStream(statsDoc, overviewPage);

        contentStream.beginText();
        contentStream.newLineAtOffset(initX + 18, initY - cellHeight + 10);
        contentStream.setFont(PDType1Font.TIMES_ROMAN, 18);
        contentStream.showText("Mean: " + mean/workingTest.getTakenTests().length);
        contentStream.endText();

        contentStream.addRect(initX, initY, cellWidth, -cellHeight);
        initX += cellWidth;

        contentStream.addRect(initX, initY, cellWidth+20, -cellHeight);

        contentStream.beginText();
        contentStream.newLineAtOffset(initX + 10, initY - cellHeight + 10);
        contentStream.setFont(PDType1Font.TIMES_ROMAN, 18);

        sort(pointArray);

        //        if (workingTest.getTakenTests().length % 2 == 0) {
        //            median = (pointArray[workingTest.getTakenTests().length/2-1] + pointArray[workingTest.getTakenTests().length/2])/2;
        //        } else {
        //            median = pointArray[(workingTest.getTakenTests().length+1) - 1];
        //        }
        if(workingTest.getTakenTests().length%2==1)
        {
            median=pointArray[(workingTest.getTakenTests().length+1)/2-1];
        }
        else
        {
            median=(pointArray[workingTest.getTakenTests().length/2-1]+pointArray[workingTest.getTakenTests().length/2])/2;
        }

        contentStream.showText("Median: " + median);
        contentStream.endText();

        contentStream.stroke();
        contentStream.close();
        statsDoc.save(statisticsPath.toString());
        statsDoc.close();
    }

    void sort(double arr[]) {
        double n = arr.length;
        for (int i = 1; i < n; ++i) {
            double key = arr[i];
            int j = i - 1;

            /* Move elements of arr[0..i-1], that are
               greater than key, to one position ahead
               of their current position */
            while (j >= 0 && arr[j] > key) {
                arr[j + 1] = arr[j];
                j = j - 1;
            }
            arr[j + 1] = key;
        }
    }

    /**
     * Exports the tests to folderPath
     * @throws IOException
     */
    private void exportTests() throws IOException {
        int testsNumber = 1;
        //for each student
        for (TakenTest test : workingTest.getTakenTests()) {
            System.out.println("Total tests: " + workingTest.getTakenTests().length);
            System.out.println("Working on: " + testsNumber);
            //create a new test that is wider than original
            //TODO: Consider A3 page size, eg double wide 8.5x11
            //Our pages right now are ~800x600 pts
            PDDocument studentTest = new PDDocument();
            //for each page
            for (int i = 0; i < workingTest.getPagesPerTest(); i++) {
                studentTest.addPage(new PDPage());
                PDPage page = studentTest.getPage(i);

                PDPageContentStream contentStream = new PDPageContentStream(studentTest, page);

                //add original image of page to left side
                System.out.println("Render Page " + (i + (workingTest.getPagesPerTest() * testsNumber - workingTest.getPagesPerTest())) + " of Original pdf");
                Image pageRenderedImage = test.getTest().renderPageImage(i + (workingTest.getPagesPerTest() * testsNumber - workingTest.getPagesPerTest()));
                BufferedImage bufferedImage = SwingFXUtils.fromFXImage(pageRenderedImage, null);
                PDImageXObject img = LosslessFactory.createFromImage(studentTest, bufferedImage);
                int scale = 20; // alter this value to set the image size
                contentStream.setStrokingColor(Color.lightGray);
                //these magic numbers are 8.5 * 2 by 11 * 2 to get 17 by 22, an int instead of float
                contentStream.addRect(20 - 3, 330 - 3, 17 * scale + 6, 22 * scale + 6);
                contentStream.closeAndStroke();
                contentStream.drawImage(img, 20, 330, 17 * scale, 22 * scale);

                int yOffset = 0;
                int pageQuestionNumber = 0;
                //for each question on page
                for (Question q : workingTest.getQuestions()) {
                    //show feedback on right
                    if (q.getPageNum() == i + 1) {
                        int spacer = (pageQuestionNumber) * 10;
                        pageQuestionNumber++;
                        //generate box does the work of creating a feedback box with all the necessary info
                        yOffset = yOffset - generateBox(contentStream, 380, 772 - yOffset - spacer, 220, test, q);
                    }
                }

                contentStream.close();
            }
            //save file to path
            studentTest.save(folderPath.toString() + "\\test_" + testsNumber + ".pdf");
            testsNumber++;
        }
    }

    /**
     * Everything involved with generating box for feedback
     *
     * @param contentStream
     * @param x
     * @param y
     * @param width
     * @param test
     * @param question
     * @return height taken by box, so we can move the next box below it
     * @throws IOException
     */
    private int generateBox(PDPageContentStream contentStream, int x, int y, int width, TakenTest test, Question question) throws IOException {
        int boxHeight = -40;
        ArrayList<String> lines = new ArrayList<>();
        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA, 10);
        contentStream.newLineAtOffset(x + 3, y - 10 - 3);
        contentStream.setLeading(13F);

        //sum all feedbacks for a point total on the question, this could be refactored into takenTest
        float totalPoints = 0;
        for (Feedback f : test.GetQuestionFeedbacks(question.getQNum() - 1)) {
            totalPoints += Float.parseFloat(f.getPoints());
        }

        //initial info line
        contentStream.showText("Question: " + question.getQNum() + "           " + totalPoints + "/" + question.getPointsPossible());
        contentStream.newLine();

        //loops through each feedback on the question and splits it into lines
        for (int i = 0; i < test.GetQuestionFeedbacks(question.getQNum() - 1).size(); i++) {
            lines.addAll(splitString("(" + test.GetQuestionFeedbacks(question.getQNum() - 1).get(i).getPoints() + ") " + test.GetQuestionFeedbacks(question.getQNum() - 1).get(i).getExplanation(), (float) width - 3));
            lines.add(" ");
        }
        //loops through lines and prints them to the page, expanding the box as we go
        for (String s : lines) {
            contentStream.showText(s);
            contentStream.newLine();
            boxHeight -= 10;
        }
        contentStream.endText();
        contentStream.addRect(x, y, width, boxHeight);
        contentStream.closeAndStroke();
        return boxHeight;
    }

    /**
     * Splits string to arraylist based on width of string
     * https://stackoverflow.com/questions/19635275/how-to-generate-multiple-lines-in-pdf-using-apache-pdfbox
     *
     * @param text  The text that should be split
     * @param width The width of the box
     * @return Arraylist of the split strings
     * @throws IOException
     */
    private ArrayList<String> splitString(String text, float width) throws IOException {
        ArrayList<String> lines = new ArrayList<String>();
        int lastSpace = -1;
        while (text.length() > 0) {
            int spaceIndex = text.indexOf(' ', lastSpace + 1);
            if (spaceIndex < 0) {
                spaceIndex = text.length();
            }
            String subString = text.substring(0, spaceIndex);
            PDType1Font pdfFont = PDType1Font.HELVETICA;
            float size = 10 * pdfFont.getStringWidth(subString) / 1000;
            //System.out.printf("'%s' - %f of %f\n", subString, size, width);
            if (size > width) {
                if (lastSpace < 0) {
                    lastSpace = spaceIndex;
                }
                subString = text.substring(0, lastSpace);
                lines.add(subString);
                text = text.substring(lastSpace).trim();
                //System.out.printf("'%s' is line\n", subString);
                lastSpace = -1;
            } else if (spaceIndex == text.length()) {
                lines.add(text);
                //System.out.printf("'%s' is line\n", text);
                text = "";
            } else {
                lastSpace = spaceIndex;
            }
        }
        return lines;
    }


    public static void setFolderPath(Path newFolder) {
        folderPath = newFolder;
        folderPathText = folderPath.toString();
    }

    public static void setFilePathText(Path newFile){
        statisticsPath = newFile;
        filePathText = statisticsPath.toString();
    }

    @FXML
    public void Export() {
        Export.simpleExport();
    }


    // Utility functions that are visible to FXML file
    @FXML
    public void OpenTest() throws IOException { PDFGrader.OpenTest(); }
    @FXML
    public void GoToSetup() throws IOException { PDFGrader.GoToSetup(); }
    @FXML
    private void Exit() { PDFGrader.Exit(); }
    @FXML
    private void OpenGithub() { PDFGrader.OpenGithub(); }
    @FXML
    private void OpenAbout() { PDFGrader.showAboutPage(); }
}
