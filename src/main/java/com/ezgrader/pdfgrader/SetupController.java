package com.ezgrader.pdfgrader;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.IntegerStringConverter;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.ezgrader.pdfgrader.PDFGrader.workingTest;

/**
 * Setup Controller works with setup.fxml to setup UI and functionality for the scene.
 */
public class SetupController {

    @FXML
    private Pagination pagination;
    @FXML
    private TextField testNameField;
    @FXML
    private Label pdfFilename;
    @FXML
    private TextField pagesField;
    @FXML
    private Label totalPoints;
    @FXML
    private Label totalTests;
    @FXML
    private TableView questionTable;
    @FXML
    private TableColumn qNumberCol;
    @FXML
    private TableColumn<Question, Double> pointsPossibleCol;
    @FXML
    private TableColumn<Question, Integer> pageNumCol;
    @FXML
    private Button addQuestionButton;
    @FXML
    private Button startGradingButton;

    @FXML
    public void initialize() {
        pagesField.setTextFormatter(TextFilters.GetIntFilter());
        //tests are initially 1 page long
        pagesField.setText(1 + "");

        setTableEditable();

        addQuestionButton.setDisable(true);
        startGradingButton.setDisable(true);

        browseForPDF();
    }

    /**
     * OnAction of Choose PDF button, open a File Chooser,
     * then setup page viewing once we get a file.
     */
    @FXML
    private void browseForPDF() {
        //open a FileChooser when ChoosePDF is clicked
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter pdfFilter = new FileChooser.ExtensionFilter("PDF files (*.pdf)", "*.pdf", "*.PDF");
        fileChooser.getExtensionFilters().add(pdfFilter);
        fileChooser.setTitle("Choose PDF");
        //Set initial directory to users downloads
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home") + System.getProperty("file.separator")+ "Downloads"));
        File pdf = fileChooser.showOpenDialog(PDFGrader.getStage().getScene().getWindow());

        if (pdf != null) {
            pdfFilename.setText(pdf.getName());
            System.out.println(pdf.getAbsoluteFile());
            // create test a new test using path from file chooser
            Path path = Paths.get(pdf.getPath());
            workingTest = new Test(path);
            totalTests.setText(workingTest.getTotalPages() + "");
            //initial page update
            updatePages();
            //set up pagination page grabbing
            pagination.setPageCount(workingTest.getTotalPages());
            pagination.setPageFactory(pageNumber -> new ImageView(workingTest.renderPageImage(pageNumber)));

            // set default test name, unless one has already been entered
            if (testNameField.getText().equals("")) {
                testNameField.setText(pdf.getName().substring(0, pdf.getName().toLowerCase().indexOf(".pdf")));
            }

            // allow adding questions
            addQuestionButton.setDisable(false);
        }
    }

    /**
     * Updates total pages per test shown in UI.
     * TODO: might be able to be solved using a SimpleStringProperty
     */
    @FXML
    public void updatePages(){
        if (workingTest != null) {
            //get int from pagesField
            String pagesPerTestString = pagesField.getText();
            try {
                int pagesPerTestInt = Integer.parseInt(pagesPerTestString);
                workingTest.setPagesPerTest(pagesPerTestInt);
                //set text for total tests
                totalTests.setText((workingTest.getTotalPages() / workingTest.getPagesPerTest()) + "");
            } catch (NumberFormatException e) {
                System.out.println("No characters in box.");
            }
        }
    }

    /**
     * Creates a new question and adds it to the test.
     * Also adds to the UI and updates the points.
     */
    @FXML
    private void addQuestion() {
        questionTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        //current page index saves an index not a page, so when displaying it might be best to add 1
        workingTest.getQuestions().add(new Question(workingTest.getQuestions().size()+1, 0.0, pagination.getCurrentPageIndex() + 1));
        questionTable.setItems(workingTest.getQuestions());
        UpdateTotalPoints();

        // Focus on new entry
        //questionTable.requestFocus();
        questionTable.getSelectionModel().select(questionTable.getItems().size()-1, pointsPossibleCol);
        questionTable.getFocusModel().focus(questionTable.getItems().size()-1, pointsPossibleCol);

        // if at least one question, allow
        startGradingButton.setDisable(false);
    }


    /**
     * When the delete key is pressed in the tableView delete the selected question.
     */
    @FXML
    public void deleteQuestion() {
        //delete the currently selected question
        workingTest.getQuestions().remove(questionTable.getSelectionModel().getSelectedItem());
        System.out.println("DELETE " + questionTable.getSelectionModel().getSelectedIndex());

        //fix the question numbers for each question
        ObservableList<Question> questions = workingTest.getQuestions();
        for (int i = 0; i < questions.size(); i++) {
            questions.get(i).setQNum(i+1);
        }
        UpdateTotalPoints();
        //if there are no questions in the list after the deletion then disable the start button
        if (workingTest.getQuestions().isEmpty()) {
            startGradingButton.setDisable(true);
        }
        questionTable.refresh();
    }

    /**
     * Switches scene to the grading scene.
     * @throws IOException
     */
    @FXML
    public void StartGrading() throws IOException {
        // TODO: Make testNameField into simpleStringProperty so when the field is updated the value is updated.
        workingTest.setName(testNameField.getText());
        workingTest.CreateTakenTests();
        PDFGrader.SwitchScene("grading.fxml");
    }

    /**
     * runs through each question and tallies the points
     */

    @FXML
    private void UpdateTotalPoints() {
        double total = 0;
        for (Object points : questionTable.getItems()) {
            total += (Double) pointsPossibleCol.getCellObservableValue((Question) points).getValue();
        }
        totalPoints.setText("" + total);
    }

    private void setTableEditable() {
        questionTable.setEditable(true);

        questionTable.setOnKeyPressed(event -> {
            if (event.getCode().isDigitKey()) {
                editFocusedCell();
            } else if (event.getCode() == KeyCode.DELETE) {
                deleteQuestion();
            }
        });
        questionTable.getFocusModel().focusedCellProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                TablePosition selectedCellPosition = questionTable.getFocusModel().getFocusedCell();
                Platform.runLater(() -> questionTable.edit(selectedCellPosition.getRow(), selectedCellPosition.getTableColumn()));
            }
        });
        // This is the only property I could find where this triggers at the right
        // time, seems fine to do
        questionTable.focusedProperty().addListener(event -> {
            UpdateTotalPoints();
        });
        // use custom editable cells
        pointsPossibleCol.setCellFactory(EditableTableCell.<Question, Double>forTableColumn(new DoubleStringConverter(), TextFilters.doubleRegex));
        pageNumCol.setCellFactory(EditableTableCell.<Question, Integer>forTableColumn(new IntegerStringConverter(), TextFilters.intRegex));
        // No sorting columns except Question Number
        pointsPossibleCol.setSortable(false);
        pageNumCol.setSortable(false);
    }

    @SuppressWarnings("unchecked")
    private void editFocusedCell() {
        final TablePosition<Question, ?> focusedCell = (TablePosition<Question, ?>) ((TableView.TableViewFocusModel)questionTable
                .focusModelProperty().get()).focusedCellProperty().get();
        questionTable.edit(focusedCell.getRow(), focusedCell.getTableColumn());
    }

    @SuppressWarnings("unchecked")
    private void selectPrevious() {
        if (questionTable.getSelectionModel().isCellSelectionEnabled()) {
            // in cell selection mode, we have to wrap around, going from
            // right-to-left, and then wrapping to the end of the previous line
            TablePosition<Question, ?> pos = questionTable.getFocusModel()
                    .getFocusedCell();
            if (pos.getColumn() - 1 >= 0) {
                // go to previous row
                questionTable.getSelectionModel().select(pos.getRow(),
                        getTableColumn(pos.getTableColumn(), -1));
            } else if (pos.getRow() < questionTable.getItems().size()) {
                // wrap to end of previous row
                questionTable.getSelectionModel().select(pos.getRow() - 1,
                        questionTable.getVisibleLeafColumn(
                                questionTable.getVisibleLeafColumns().size() - 1));
            }
        } else {
            int focusIndex = questionTable.getFocusModel().getFocusedIndex();
            if (focusIndex == -1) {
                questionTable.getSelectionModel().select(questionTable.getItems().size() - 1);
            } else if (focusIndex > 0) {
                questionTable.getSelectionModel().select(focusIndex - 1);
            }
        }
    }

    private TableColumn<Question, ?> getTableColumn(
            final TableColumn<Question, ?> column, int offset) {
        int columnIndex = questionTable.getVisibleLeafIndex(column);
        int newColumnIndex = columnIndex + offset;
        return questionTable.getVisibleLeafColumn(newColumnIndex);
    }
}
