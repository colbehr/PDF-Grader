package com.ezgrader.pdfgrader;

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
    private Path path;
    private int totalPages;
    private int pagesPerTest;

    private String name;
    private ObservableList<Question> questions;
    private TakenTest[] takenTests;

    /**
     * Initializes a document and renderer for the PDF at path.
     * @param path
     */
    public Test(Path path){
        this.path = path;
        try {
            document = PDDocument.load(path.toFile());
            this.totalPages = document.getNumberOfPages();
            renderer = new PDFRenderer(document);
        } catch (IOException e) {
            throw new UncheckedIOException("Issue loading " + path, e);
        }
        questions = FXCollections.observableArrayList();
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
            image = renderer.renderImage(pageIndex, 1);
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
            takenTests[i] = new TakenTest(this);
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
}
