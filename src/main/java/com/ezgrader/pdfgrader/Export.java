package com.ezgrader.pdfgrader;

import javafx.embed.swing.JFXPanel;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;

import static com.ezgrader.pdfgrader.PDFGrader.getStage;
import static com.ezgrader.pdfgrader.PDFGrader.workingTest;

public class Export {

    private static Path folderPath;

    public static void simpleExport() {
        if (folderPath == null || !folderPath.toFile().exists()) browseAndSetExportFolder();

        if (folderPath != null && folderPath.toFile().exists()) {
            try {
                exportFiles();
                Toast.Notification("Successfully exported " + workingTest.getName());
            } catch (IOException e) {
                Toast.Error("Could not export tests. Try again.");
            }
        }
    }

    /**
     * Exports overview and individual tests, then returns the user to the home screen
     *
     */
    private static void exportFiles() throws IOException {
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
        int currPage = 0;
        PDPage thisPage = statsDoc.getPage(currPage);
        currPage++;

        int pageHeight = (int) thisPage.getTrimBox().getHeight();
        int pageWidth = (int) thisPage.getTrimBox().getWidth();

        PDPageContentStream contentStream = new PDPageContentStream(statsDoc, thisPage);

        TakenTest[] takenTests = workingTest.getTakenTests();

        //creating table
        contentStream.setStrokingColor(Color.DARK_GRAY);
        contentStream.setLineWidth(1);

        int initX = 50;
        int initY = pageHeight - 50;
        int cellHeight = 20;
        int cellWidth = 130;
        int fontSize = 14;

        int currX = initX;
        int currY = initY - cellHeight + 10;
        int tableCounter = 1;
        double pointArray[];
        pointArray = new double[takenTests.length];
        double mean = 0;
        double median;

        // STUDENT SCORES /////////////////////////////////
        int colCount = workingTest.getQuestions().size() + 2;
        int rowCount = takenTests.length;

        currY = csPrintLine(workingTest.getName() + " - Overview", contentStream, currX, currY, cellWidth, cellHeight, fontSize, true, 2);

        currX = csPrint("Student", contentStream, currX, currY, cellWidth, cellHeight, fontSize, true);

        currY = csPrintLine("Total Score", contentStream, currX, currY, cellWidth, cellHeight, fontSize, true, 2);
        currX = initX;

        for (int i = 1; i <= rowCount; i++) {
            if (tableCounter == 23) {
                contentStream.stroke();
                contentStream.close();
                tableCounter = 1;
                currX = initX;
                currY = initY;
                statsDoc.addPage(new PDPage());
                thisPage = statsDoc.getPage(currPage);
                currPage++;
                contentStream = new PDPageContentStream(statsDoc, thisPage);
            }
            currX = csPrint(takenTests[i-1].getId(), contentStream, currX, currY, cellWidth, cellHeight, fontSize, false);

            for (int j = 1; j <= 2; j++) {
                if (j == 2) {
                    double totalPoints = takenTests[i - 1].GetTotalPoints();
                    pointArray[i-1] = takenTests[i - 1].GetTotalPoints();
                    mean += totalPoints;

                    currY = csPrintLine(String.valueOf(totalPoints), contentStream, currX, currY, cellWidth, cellHeight, fontSize, false, 1);
                    currX = initX;
                }
            }
            tableCounter++;
        }
        mean = Math.round(mean/takenTests.length * 100.0) / 100.0; // 2 decimal places

        //TODO: seperate table for other stats (mean, median, more???)
        contentStream.stroke();
        contentStream.close();
        statsDoc.addPage(new PDPage());
        PDPage overviewPage = statsDoc.getPage(currPage);
        contentStream = new PDPageContentStream(statsDoc, overviewPage);

        currX = initX;
        currY = initY;

        // OVERALL STATS //////////////////////////////////
        // Mean
        currY = csPrintLine("OVERALL SCORE STATISTICS", contentStream, currX, currY, cellWidth, cellHeight, fontSize, true, 1);
        currX = initX;

        currX = csPrint("Mean: " + mean, contentStream, currX, currY, cellWidth, cellHeight, fontSize, false);

        // Median
        sort(pointArray);
        if (takenTests.length%2==1) {
            median = pointArray[(takenTests.length+1)/2-1];
        } else {
            median = (pointArray[takenTests.length/2-1]+pointArray[takenTests.length/2])/2;
            median = Math.round(median * 100.0) / 100.0; // 2 decimal places
        }

        currX = csPrint("Median: " + median, contentStream, currX, currY, cellWidth, cellHeight, fontSize, false);

        // Std Deviation
        double sqrDistSum = 0;
        for (int i = 0; i < takenTests.length; i++) {
            double score = takenTests[i].GetTotalPoints();
            sqrDistSum += Math.pow(score - mean, 2);
        }
        double stdDev = Math.sqrt(sqrDistSum / takenTests.length);
        stdDev = Math.round(stdDev * 100.0) / 100.0; // 2 decimal places

        currY = csPrintLine("Std Deviation: " + stdDev, contentStream, currX, currY, cellWidth, cellHeight, fontSize, false, 2);
        currX = initX;

        // INDIVIDUAL QUESTION STATS //////////////////////
        currY = csPrintLine("PER QUESTION STATISTICS", contentStream, currX, currY, cellWidth, cellHeight, fontSize, true, 1);
        currX = initX;

        // Calculate everything upfront
        int questionCount = workingTest.getQuestions().size();
        int studentCount = takenTests.length;

        double[] qMedians = new double[questionCount];
        double[] qMeans = new double[questionCount];
        double[] qStdDevs = new double[questionCount];

        for (int i = 0; i < questionCount; i++) {
            double[] studentPoints = new double[takenTests.length];
            double studentPointsSum = 0;

            for (int j = 0; j < takenTests.length; j++) {
                double pointsGiven = takenTests[j].GetQuestionPointsGiven(i);
                studentPoints[j] = pointsGiven;
                studentPointsSum += pointsGiven;
            }
            // Median
            sort(studentPoints);
            if (takenTests.length % 2 == 1) {
                qMedians[i] = studentPoints[(studentCount+1)/2-1];
            } else {
                qMedians[i] = (studentPoints[studentCount/2-1]+studentPoints[studentCount/2]) / 2;
                qMedians[i] = Math.round(qMedians[i] * 100.0) / 100.0; // 2 decimal places
            }
            // Mean
            qMeans[i] = studentPointsSum / studentCount;
            qMeans[i] = Math.round(qMeans[i] * 100.0) / 100.0; // 2 decimal places
            // Std Deviation
            sqrDistSum = 0; // reuse variable from general std dev
            for (double p : studentPoints) {
                sqrDistSum += Math.pow(p - qMeans[i], 2);
            }
            qStdDevs[i] = Math.sqrt(sqrDistSum / studentCount);
            qStdDevs[i] = Math.round(qStdDevs[i] * 100.0) / 100.0; // 2 decimal places
        }

        // Write to stream
        for (int i = 0; i < questionCount; i++) {
            currY = csPrintLine("Question " + (i+1), contentStream, currX, currY, cellWidth, cellHeight, fontSize, true, 1);
            currX = initX;

            currX = csPrint("Mean: " + qMeans[i], contentStream, currX, currY, cellWidth, cellHeight, fontSize, false);

            currX = csPrint("Median: " + qMedians[i], contentStream, currX, currY, cellWidth, cellHeight, fontSize, false);

            currY = csPrintLine("Std Deviation: " + qStdDevs[i], contentStream, currX, currY, cellWidth, cellHeight, fontSize, false, 1);
            currX = initX;
        }

        contentStream.stroke();
        contentStream.close();


        //TODO: TEASDFA
        statsDoc.addPage(new PDPage());

        currPage++;
        thisPage = statsDoc.getPage(currPage);
        contentStream = new PDPageContentStream(statsDoc, thisPage);

        createChart(pointArray, folderPath.toString(), 1, mean/workingTest.getTakenTests().length, median); //creates chart image to add
        PDImageXObject pdImage = PDImageXObject.createFromFile(folderPath.toString() + "/a.png", statsDoc);
        contentStream.drawImage(pdImage, 50, 566);
        File toDelete = new File(folderPath.toString() + "/a.png");
        toDelete.delete(); //deletes chart image

        createChart(pointArray, folderPath.toString(), 2, mean/workingTest.getTakenTests().length, median); //creates chart image to add
        pdImage = PDImageXObject.createFromFile(folderPath.toString() + "/a.png", statsDoc);
        contentStream.drawImage(pdImage, 50, 316);
        toDelete.delete();

        createChart(pointArray, folderPath.toString(), 3, mean/workingTest.getTakenTests().length, median); //creates chart image to add
        pdImage = PDImageXObject.createFromFile(folderPath.toString() + "/a.png", statsDoc);
        contentStream.drawImage(pdImage, 50, 66);
        toDelete.delete();

        contentStream.stroke();
        contentStream.close();

        statsDoc.save(getOverviewPath().toString());
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

                if (i == 0) { // TOTAL SCORE
                    int margin = 15;
                    int offsetX = csPrint("Total Score: ", contentStream, margin, (int) page.getTrimBox().getHeight() - margin, 100, 50, 14, false);
                    double percent = Math.round(test.GetTotalPoints() / test.GetTotalPointsPossible() * 10.0) * 10.0;
                    String scoreText = test.GetTotalPoints() +  " / " + test.GetTotalPointsPossible() + " \u2014 " + percent + "%";
                    csPrintLine(scoreText, contentStream, offsetX, (int) page.getTrimBox().getHeight() - margin, 100, 50, 14, true);
                }

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

    private static int csPrint(String text, PDPageContentStream contentStream, int x, int y, int w, int h, int fontSize, boolean boldText) throws IOException {
        contentStream.beginText();
        contentStream.newLineAtOffset(x + 10, y);
        if (boldText) {
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, fontSize);
        } else {
            contentStream.setFont(PDType1Font.HELVETICA, fontSize);
        }
        contentStream.showText(text);
        contentStream.endText();

        //contentStream.addRect(x, y, w, -h);
        x += w;
        //contentStream.addRect(x, y, w, -h);

        return x;
    }

    private static int csPrintLine(String text, PDPageContentStream contentStream, int x, int y, int w, int h, int fontSize, boolean boldText, int lineSpacing) throws IOException {
        csPrint(text, contentStream, x, y, w, h, fontSize, boldText);
        return y - h * lineSpacing;
    }

    private static int csPrintLine(String text, PDPageContentStream contentStream, int x, int y, int w, int h, int fontSize, boolean boldText) throws IOException {
        return csPrintLine(text, contentStream, x, y, w, h, fontSize, boldText, 1);
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
            if (f.getPoints().length() > 0) totalPoints += Float.parseFloat(f.getPoints());
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

    public static void browseAndSetExportFolder() {
        //open a FileChooser when ChoosePDF is clicked
        DirectoryChooser folderChooser = new DirectoryChooser();
        folderChooser.setTitle("Choose a folder to save test results");
        //Set initial directory to previously set path, otherwise to user's Desktop
        if (Export.getFolderPath() != null) {
            folderChooser.setInitialDirectory(Export.getFolderPath().toFile());
        } else {
            folderChooser.setInitialDirectory(new File(System.getProperty("user.home") + System.getProperty("file.separator") + "Desktop"));
        }
        File folder = folderChooser.showDialog(getStage());
        if (folder != null) {
            folderPath = Paths.get(folder.getPath());
        }
    }

    public static void setExportFolder(Path path) {
        folderPath = path;
    }

    public static Path getFolderPath() {
        return folderPath;
    }

    public static Path getOverviewPath() {
        if (workingTest == null) return null;

        return Paths.get(folderPath + "/" + workingTest.getName() + "-OVERVIEW.pdf");
    }
    public static void createChart(double[] pointsArray, String path, int type, double mean, double median) throws IOException {
        new JFXPanel();
        final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Students");
        final LineChart<String,Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle("Overall test results");
        XYChart.Series<String, Number> series = new XYChart.Series<String, Number>();
        XYChart.Series<String, Number> series2 = new XYChart.Series<String, Number>();
        XYChart.Series<String, Number> series3 = new XYChart.Series<String, Number>();
        series2.setName("Mean");
        series.setName("test3");
        switch (type) {
            case 1:
                for (int i = 0; i < workingTest.getTakenTests().length; i++) {
                    series.getData().add(new XYChart.Data<>(Integer.toString(i), pointsArray[i]));
                }
                chart.getData().add(series);
                break;
            case 2:
                for (int i = 0; i < workingTest.getTakenTests().length; i++) {
                    series.getData().add(new XYChart.Data<>(Integer.toString(i), pointsArray[i]));
                }
                for (int j = 0; j < workingTest.getTakenTests().length; j++) {
                    series2.getData().add(new XYChart.Data<>(Integer.toString(j), mean));
                }
                chart.getData().add(series);
                chart.getData().add(series2);
                break;
            case 3:
                for (int i = 0; i < workingTest.getTakenTests().length; i++) {
                    series.getData().add(new XYChart.Data<>(Integer.toString(i), pointsArray[i]));
                }
                for (int i = 0; i < workingTest.getTakenTests().length; i++) {
                    series3.getData().add(new XYChart.Data<>(Integer.toString(i), median));
                }

                chart.getData().add(series);
                chart.getData().add(series3);
                break;
        }
        chart.setAnimated(false);
        Stage stage = new Stage();
        Scene scene = new Scene(chart, 515, 200);
        stage.setScene(scene);
        WritableImage img = new WritableImage(515, 200);
        scene.snapshot(img);

        File file = new File(Paths.get(path, "a.png").toString());
        try {
            ImageIO.write(SwingFXUtils.fromFXImage(img, null), "png", file);
        } catch (IOException e) {
            //logger.error("Error occurred while writing the chart image
        }
    }

}
