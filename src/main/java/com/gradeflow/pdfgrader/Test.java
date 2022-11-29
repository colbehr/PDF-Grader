package com.gradeflow.pdfgrader;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;

/**
 * Test stores all data related to the test and holds individual test data of students in takenTests.
 */
public class Test {
    private PDDocument document;
    private PDFRenderer renderer;
    private Path pdfPath;
    private int totalPages;
    private int pagesPerTest;
    private String name;
    private ObservableList<Question> questions;
    private TakenTest[] takenTests;

    // For a saved then reloaded test, where grading left off
    // [0] = question number, [1] = taken test number
    private int savedPlace[];
    public Path savePath;

    /**
     * Initializes a document and renderer for the PDF at path.
     * @param pdfPath
     */
    public Test(Path pdfPath){
        this.pdfPath = pdfPath;
        try {
            document = PDDocument.load(pdfPath.toFile());
            this.totalPages = document.getNumberOfPages();
            renderer = new PDFRenderer(document);
        } catch (IOException e) {
            Toast.Error("Error loading PDF");
            //throw new UncheckedIOException("Issue loading " + pdfPath, e);
        }
        questions = FXCollections.observableArrayList();
        savedPlace = new int[2]; // default to question 0, taken test 0
    }

    /**
     * Takes an index, or pageNumber-1,
     * and returns an Image of the page to use in JavaFX with an ImageView.
     * @param pageIndex
     * @return Image of the page
     */
    public Image renderPageImage(int pageIndex){
        BufferedImage image;
        try{
            image = renderer.renderImage(pageIndex, 2);
        } catch (IOException e) {
            throw new UncheckedIOException("Issue rendering page " + pageIndex, e);
        }
        return SwingFXUtils.toFXImage(image, null);
    }

    /**
     * Creates new TakenTests for each student test in the PDF.
     */
    public void CreateTakenTests() {
        takenTests = new TakenTest[totalPages / pagesPerTest];
        for (int i = 0; i < takenTests.length; i++) {
            takenTests[i] = new TakenTest(this, "test_" + i);
        }

    }

    /**
     * Closes PDDocument, used when this test is being discarded.
     */
    public void CloseDocument() {
        try {
            document.close();
        } catch (IOException e) {
            System.out.println("Could not close document");
        }
    }

    /**
     * Setter for pagesPerTest, set by user.
     * @param pagesPerTest
     */
    public void setPagesPerTest(int pagesPerTest) {
        this.pagesPerTest = pagesPerTest;
    }

    /**
     * Setter for name, set by user.
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the total pages of pdf.
     * @return
     */
    public int getTotalPages(){
        return this.totalPages;
    }

    /**
     * Gets pages per individual test set by user.
     * @return
     */
    public int getPagesPerTest() {
        return pagesPerTest;
    }

    /**
     * Gets list of questions.
     * @return
     */
    public ObservableList<Question> getQuestions() {
        return questions;
    }

    /**
     * Gets array of taken tests.
     * @return
     */
    public TakenTest[] getTakenTests() {
        return takenTests;
    }

    /**
     * Gets the name of the test set by the user.
     * @return
     */
    public String getName() {
        return name;
    }

    public Path getPdfPath() {
        return pdfPath;
    }

    public void setSavedPlace(int question, int takenTest) {
        savedPlace[0] = question;
        savedPlace[1] = takenTest;
    }

    public int[] getSavedPlace() {
        return savedPlace;
    }
}
