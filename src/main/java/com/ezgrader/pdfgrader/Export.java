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
import java.util.ArrayList;

import static com.ezgrader.pdfgrader.PDFGrader.workingTest;

/**
 * Export Controller works with export.fxml to set up UI and functionality for the scene.
 */
public class Export {

    private static Path statisticsPath;
    private static Path folderPath;

    public static void simpleExport() {
        DirectoryChooser folderChooser = new DirectoryChooser();
        folderChooser.setTitle("Choose a folder to save graded files");
        //Set initial directory to users Desktop
        folderChooser.setInitialDirectory(new File(System.getProperty("user.home") + System.getProperty("file.separator") + "Desktop"));
        File exportDir = folderChooser.showDialog(PDFGrader.getStage().getScene().getWindow());
        if (exportDir != null) {
            statisticsPath = Paths.get(exportDir.getPath() + "/" + workingTest.getName() + "-OVERVIEW.pdf");
            folderPath = Paths.get(exportDir.getPath());
            System.out.println(statisticsPath);
            System.out.println(folderPath);

            try {
                exportFiles(null);
                Toast.Notification("Successfully exported " + workingTest.getName());
            } catch (IOException e) {
                Toast.Error("Could not export tests. Try again.");
            }
        }
    }

    /**
     * Exports files when export button is pressed
     *
     * @param event
     */
    private static void exportFiles(ActionEvent event) throws IOException {
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
        System.out.println(statisticsPath + "   --   " + folderPath);
        exportStats();
        exportTests();

        try {
            PDFGrader.SwitchScene("home.fxml");
        } catch (IOException e) {
            System.exit(0);
        }
    }

    private static void exportStats() throws IOException {
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

    static void sort(double arr[]) {
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
    static private void exportTests() throws IOException {
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
            studentTest.save(folderPath.toString() + "/test_" + testsNumber + ".pdf");
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
    static private int generateBox(PDPageContentStream contentStream, int x, int y, int width, TakenTest test, Question question) throws IOException {
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
    static private ArrayList<String> splitString(String text, float width) throws IOException {
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
}
