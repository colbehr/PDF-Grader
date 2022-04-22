package com.ezgrader.pdfgrader;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.IntegerStringConverter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SetupController {
    private File pdf;
    private ObservableList<Question> questionTest = FXCollections.observableArrayList(); // temporary testing list
    private Test test;
    @FXML
    private ImageView pdfView;
    @FXML
    private Pagination pagination;
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
    public void initialize() {
        pagesField.setTextFormatter(TextFilters.GetIntFilter());
        //tests are initially 1 page long
        pagesField.setText(1 + "");
        questionTable.setEditable(true);
        pointsPossibleCol.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        pageNumCol.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        // TODO: Figure out how to call UpdateTotalPoints() on table edit
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
            test = new Test(path);
            pdfView.setImage(test.renderPageImage(0));
            totalTests.setText(test.getTotalPages() + "");
            //initial page update
            updatePages();
            pagination.setPageCount(test.getTotalPages());
            pagination.setPageFactory((Integer pageIndex) -> {
                    pdfView.setImage(test.renderPageImage(pageIndex));
                    //TODO: issue with error after every page change, either image glitch or error for now
                    sideSplit2.setDividerPosition(0,sideSplit2.getDividerPositions()[0]+1);
                    return pdfView;
                }
            );
        }
    }

    @FXML
    public void updatePages(){
        //get int from box
        if (this.test != null) {
            String pagesPerTestString = pagesField.getText();
            try {
                int pagesPerTestInt = Integer.parseInt(pagesPerTestString);
                test.setPagesPerTest(pagesPerTestInt);
                //set text for total tests
                totalTests.setText((test.getTotalPages() / test.getPagesPerTest()) + "");
            } catch (NumberFormatException e) {
                System.out.println("No characters in box.");
            }
        }
    }

    @FXML
    private void addQuestion() {
        questionTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        questionTest.add(new Question(questionTest.size()+1, 0.0, 1));
        questionTable.setItems(questionTest);
        UpdateTotalPoints();
    }


    @FXML
    public void deleteQuestion(javafx.scene.input.KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.DELETE) {
            Question selectedItem = (Question) questionTable.getSelectionModel().getSelectedItem();
            questionTable.getItems().remove(selectedItem);

            for (int i = 0; i < questionTest.size(); i++) {
                questionTest.get(i).setQNum(i+1);
            }
            UpdateTotalPoints();
        }
    }

    @FXML
    public void GoToGrading(ActionEvent event) throws IOException {
        Stage stage = (Stage) ((Node)event.getSource()).getScene().getWindow();
        GridPane gradingRoot = FXMLLoader.load(getClass().getResource("grading.fxml"));
        Main.MakeStretchy(gradingRoot);
        stage.setScene(new Scene(gradingRoot));

    }

    @FXML private void UpdateTotalPoints() { // not properly functional yet
        double total = 0;
        for (Object points : questionTable.getItems()) {
            total += (Double) pointsPossibleCol.getCellObservableValue(points).getValue();
        }
        totalPoints.setText("" + total);
    }
}
