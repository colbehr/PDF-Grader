package com.ezgrader.pdfgrader;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.stage.FileChooser;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.IntegerStringConverter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.ezgrader.pdfgrader.Main.workingTest;

public class SetupController {
    private File pdf;
    //private Test test;
    @FXML
    private ImageView pdfView;
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
    private SplitPane sideSplit2;
    @FXML
    private TableView questionTable;
    @FXML
    private TableColumn qNumberCol;
    @FXML
    private TableColumn pointsPossibleCol;
    @FXML
    private TableColumn pageNumCol;
    @FXML
    private Button addQuestionButton;
    @FXML
    private Button startGradingButton;

    @FXML
    public void initialize() {
        pagesField.setTextFormatter(TextFilters.GetIntFilter());
        //tests are initially 1 page long
        pagesField.setText(1 + "");
        questionTable.setEditable(true);
        pointsPossibleCol.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        pageNumCol.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        // TODO: Figure out how to call UpdateTotalPoints() on table edit

        addQuestionButton.setDisable(true);
        startGradingButton.setDisable(true);
    }

    @FXML
    private void browseForPDF(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter pdfFilter = new FileChooser.ExtensionFilter("PDF files (*.pdf)", "*.pdf");
        fileChooser.getExtensionFilters().add(pdfFilter);
        fileChooser.setTitle("Choose PDF");
        pdf = fileChooser.showOpenDialog(((Node)event.getSource()).getScene().getWindow());
        if (pdf != null) {
            pdfFilename.setText(pdf.getName());
            System.out.println(pdf.getAbsoluteFile());
            // create test
            Path path = Paths.get(pdf.getPath());
            workingTest = new Test(path);
            totalTests.setText(workingTest.getTotalPages() + "");
            //initial page update
            updatePages();
            pagination.setPageCount(workingTest.getTotalPages());
            //sets up pagination page grabbing
            pagination.setPageFactory(pageNumber -> new ImageView(workingTest.renderPageImage(pageNumber)));

            // set default test name, unless one has already been entered
            if (testNameField.getText().equals("")) {
                testNameField.setText(pdf.getName().substring(0, pdf.getName().indexOf(".pdf")));
            }

            // allow adding questions
            addQuestionButton.setDisable(false);
        }
    }

    @FXML
    public void updatePages(){
        //get int from box
        if (workingTest != null) {
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

    @FXML
    private void addQuestion() {
        questionTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        workingTest.getQuestions().add(new Question(workingTest.getQuestions().size()+1, 0.0, 1));
        questionTable.setItems(workingTest.getQuestions());
        UpdateTotalPoints();

        // if at least one question, allow
        startGradingButton.setDisable(false);
    }


    @FXML
    public void deleteQuestion(javafx.scene.input.KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.DELETE) {
            Question selectedItem = (Question) questionTable.getSelectionModel().getSelectedItem();
            questionTable.getItems().remove(selectedItem);

            for (int i = 0; i < workingTest.getQuestions().size(); i++) {
                workingTest.getQuestions().get(i).setQNum(i+1);
            }
            UpdateTotalPoints();

            if (workingTest.getQuestions().isEmpty()) {
                startGradingButton.setDisable(true);
            }
        }
    }

    @FXML
    public void StartGrading(ActionEvent event) throws IOException {
        workingTest.setName(testNameField.getText());
        workingTest.CreateTakenTests();
        Main.SwitchScene("grading.fxml");
    }

    @FXML private void UpdateTotalPoints() {
        double total = 0;
        for (Object points : questionTable.getItems()) {
            total += (Double) pointsPossibleCol.getCellObservableValue(points).getValue();
        }
        totalPoints.setText("" + total);
    }
}
