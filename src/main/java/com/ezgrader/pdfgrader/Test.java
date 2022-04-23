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
import java.util.ArrayList;
import java.util.List;


public class Test {
    private PDDocument document;
    private PDFRenderer renderer;
    private Path path;
    private int totalPages;
    private int pagesPerTest;

    private String name;
    private ObservableList<Question> questions;
    private TakenTest[] takenTests;


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

    public Image renderPageImage(int pageIndex){
        BufferedImage image;
        try{
            image = renderer.renderImage(pageIndex);
        } catch (IOException e) {
            throw new UncheckedIOException("Issue rendering page " + pageIndex, e);
        }
        return SwingFXUtils.toFXImage(image, null);
    }

    public Image renderQuestionPageImage(int question) {
        return renderPageImage(questions.get(question).getPageNum());
    }

    public void CreateTakenTests() {
        takenTests = new TakenTest[totalPages / pagesPerTest];
        for (int i = 0; i < takenTests.length; i++) {
            takenTests[i] = new TakenTest(this);
        }
    }

    public int getTotalPages(){
        return this.totalPages;
    }

    public void setPagesPerTest(int pagesPerTest) {
        this.pagesPerTest = pagesPerTest;
    }

    public int getPagesPerTest() {
        return pagesPerTest;
    }

    public ObservableList<Question> getQuestions() {
        return questions;
    }

    public TakenTest[] getTakenTests() {
        return takenTests;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
