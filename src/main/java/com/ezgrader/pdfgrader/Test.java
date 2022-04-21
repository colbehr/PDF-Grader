package com.ezgrader.pdfgrader;


import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;


public class Test {
    private PDDocument document;
    private PDFRenderer renderer;
    private Path path;
    private int totalPages;
    private int pagesPerTest;


    public Test(Path path){
        this.path = path;
        try {
            document = PDDocument.load(path.toFile());
            this.totalPages = document.getNumberOfPages();
            renderer = new PDFRenderer(document);
        } catch (IOException e) {
            throw new UncheckedIOException("Issue loading " + path, e);
        }
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

    public int getTotalPages(){
        return this.totalPages;
    }
    public void setPagesPerTest(int pagesPerTest) {
        this.pagesPerTest = pagesPerTest;
    }

    public int getPagesPerTest() {
        return pagesPerTest;
    }
}
