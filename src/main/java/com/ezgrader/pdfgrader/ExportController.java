package com.ezgrader.pdfgrader;

import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
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
     *
     * @param event
     */
    @FXML
    private void browseForTestFolder(ActionEvent event) {
        //open a FileChooser when ChoosePDF is clicked
        DirectoryChooser folderChooser = new DirectoryChooser();
        folderChooser.setTitle("Choose a folder to save graded files");
        //Set initial directory to users Desktop
        folderChooser.setInitialDirectory(new File(System.getProperty("user.home") + System.getProperty("file.separator") + "Desktop"));
        File pdf = folderChooser.showDialog(((Node) event.getSource()).getScene().getWindow());
        folderPath = Paths.get(pdf.getPath());
        folderPathText.setText(folderPath.toString());
    }

    /**
     * Opens a new file chooser for saving a statistics file
     *
     * @param event
     */
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
        File pdf = fileChooser.showSaveDialog(((Node) event.getSource()).getScene().getWindow());
        statisticsPath = Paths.get(pdf.getPath());
        filePathText.setText(statisticsPath.toString());
    }

    /**
     * exports files when export button is pressed
     *
     * @param event
     */
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
        System.out.println("Exported statistics");
        PDDocument statsDoc = new PDDocument();
        statsDoc.addPage(new PDPage());
        PDPage pageOne = statsDoc.getPage(0);

        int pageHeight = (int) pageOne.getTrimBox().getHeight();
        int pageWidth = (int) pageOne.getTrimBox().getWidth();

        PDPageContentStream contentStream = new PDPageContentStream(statsDoc, pageOne);

        //creating table
        contentStream.setStrokingColor(Color.DARK_GRAY);
        contentStream.setLineWidth(1);

        int initX = 50;
        int initY = pageHeight - 50;
        int cellHeight = 30;
        int cellWidth = 100;

        int colCount = workingTest.getQuestions().size() + 2;
        int rowCount = workingTest.getTakenTests().length;

        for (int i = 1; i <= rowCount; i++) {

            contentStream.beginText();
            contentStream.newLineAtOffset(initX + 10, initY - cellHeight + 10);
            contentStream.setFont(PDType1Font.TIMES_ROMAN, 18);
            //TODO: Figure out a way to label the students correctly
            contentStream.showText("Student " + i);
            contentStream.endText();

            for (int j = 1; j <= colCount; j++) {
                contentStream.addRect(initX, initY, cellWidth, -cellHeight);
                //TODO: fill in points for each student
                initX += cellWidth;
            }
            initX = 50;
            initY -= cellHeight;
        }

        contentStream.stroke();
        contentStream.close();
        statsDoc.save(statisticsPath.toString());
        statsDoc.close();


        //TODO: export tests
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
                        yOffset = yOffset - generateBox(contentStream, 380, 772 - yOffset - spacer,220, test, q);
                    }
                }

                contentStream.close();
            }
            //save file to path
            studentTest.save(folderPath.toString() + "\\test_" + testsNumber + ".pdf");
            testsNumber++;
        }
    }

    private int generateBox(PDPageContentStream contentStream, int x, int y, int width, TakenTest test, Question question) throws IOException {
        int height = -40;
        contentStream.addRect(x, y, width, height);
        contentStream.closeAndStroke();
        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA, 10);
        contentStream.newLineAtOffset(x + 3, y - 10 - 3);
        contentStream.setLeading(13F);
        contentStream.showText("Question: " + question.getQNum());
        contentStream.newLine();
        //TODO: have to be able to handle multiple explanations per question
        contentStream.showText(test.GetQuestionFeedbacks(question.getQNum() - 1).get(0).getExplanation());
        contentStream.endText();
        return height;
    }


}
