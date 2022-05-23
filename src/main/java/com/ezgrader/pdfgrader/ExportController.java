package com.ezgrader.pdfgrader;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.ezgrader.pdfgrader.PDFGrader.workingTest;

/**
 * Export Controller works with export.fxml to set up UI and functionality for the scene.
 */
public class ExportController {

    private Path statisticsPath;
    private Path folderPath;

    @FXML
    private javafx.scene.control.Label folderPathText;
    @FXML
    private javafx.scene.control.Label filePathText;


    /**
     * opens a browser to find a folder to put tests into
     * @param event
     */
    @FXML
    private void browseForTestFolder(ActionEvent event) {
        //open a FileChooser when ChoosePDF is clicked
        DirectoryChooser folderChooser = new DirectoryChooser();
        folderChooser.setTitle("Choose a folder to save graded files");
        //Set initial directory to users Desktop
        folderChooser.setInitialDirectory(new File(System.getProperty("user.home") + System.getProperty("file.separator")+ "Desktop"));
        File pdf = folderChooser.showDialog(((Node) event.getSource()).getScene().getWindow());
        folderPath = Paths.get(pdf.getPath());
        folderPathText.setText(folderPath.toString());
    }

    /**
     * Opens a new file chooser for saving a statistics file
     * @param event
     */
    @FXML
    private void browseForStatistics(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter pdfFilter = new FileChooser.ExtensionFilter("PDF files (*.pdf)", "*.pdf", "*.PDF");
        fileChooser.getExtensionFilters().add(pdfFilter);
        fileChooser.setTitle("Choose a location to save statistics overview");
        //Set initial directory to users Desktop
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home") + System.getProperty("file.separator")+ "Desktop"));
        if (workingTest != null) {
            fileChooser.setInitialFileName(workingTest.getName() + "_statistics.pdf");
        } else{
            fileChooser.setInitialFileName("grade_statistics.pdf");
        }
        File pdf = fileChooser.showSaveDialog(((Node) event.getSource()).getScene().getWindow());
        statisticsPath = Paths.get(pdf.getPath());
        filePathText.setText(statisticsPath.toString());
    }

    /**
     * exports files when export button is pressed
     * @param event
     */
    @FXML
    private void exportFiles(ActionEvent event) throws IOException {
        if (statisticsPath == null || folderPath == null){
            String errorMessage = "";
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Path has not been set");
            if (statisticsPath == null){
                errorMessage = "Please find a location for the statistics file.\n\n";
            }
            if (folderPath == null){
                errorMessage += "Please find a folder for graded files.";
            }
            alert.setContentText(errorMessage);
            alert.showAndWait();
            return;
        }
        System.out.println("Exported statistics");
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
        int initY = pageHeight-50;
        int cellHeight = 30;
        int cellWidth = 100;
        int tableCounter = 1;

        int rowCount = workingTest.getTakenTests().length;

        contentStream.beginText();
        contentStream.newLineAtOffset(initX + 18, initY - cellHeight + 10);
        contentStream.setFont(PDType1Font.TIMES_ROMAN, 18);
        //TODO: Figure out a way to label the students correctly
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

        //TODO: add new page when row count exceeds certain limit, or at least check if it does this automatically
        for (int i = 1; i <= rowCount; i++) {
            if (tableCounter == 23) {
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
        statsDoc.save(statisticsPath.toString());
        statsDoc.close();

        System.out.println("Exported students tests");
        //TODO: export tests

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
}
