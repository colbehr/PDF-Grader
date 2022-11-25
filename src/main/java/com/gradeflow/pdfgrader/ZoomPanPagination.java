package com.gradeflow.pdfgrader;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.Pagination;
import javafx.scene.image.ImageView;

import java.util.ArrayList;
import java.util.List;

import static com.gradeflow.pdfgrader.PDFGrader.getStage;
import static com.gradeflow.pdfgrader.PDFGrader.workingTest;

public class ZoomPanPagination extends Pagination {
    private List<ImageView> pageImages;
    private Double lastDragX = 0.0;
    private Double lastDragY = 0.0;
    private Double panX;
    private Double panY;
    private Double zoomLevel;
    private static final Double zoomSensitivity = 0.005;

    public ZoomPanPagination() {
        reSetup();
    }

    public void reSetup() {
        pageImages = new ArrayList<>();

        if (workingTest != null) {
            for (int i = 0; i < workingTest.getTotalPages(); i++) {
                ImageView imageView = new ImageView(workingTest.renderPageImage(i));
                pageImages.add(imageView);
            }
            // PANNING
            this.setOnMousePressed((e) -> {
                lastDragX = e.getX();
                lastDragY = e.getY();
            });
            this.setOnMouseDragged((e) -> {
                int page = this.getCurrentPageIndex();
                Double deltaX = e.getX() - lastDragX;
                Double deltaY = e.getY() - lastDragY;
                panX = Double.min(Double.max(panX + deltaX, -this.getWidth()), this.getWidth());
                panY = Double.min(Double.max(panY + deltaY, -this.getHeight()), this.getHeight());
                pageImages.get(page).setTranslateX(panX);
                pageImages.get(page).setTranslateY(panY);

                lastDragX = e.getX();
                lastDragY = e.getY();
            });
            // ZOOM
            this.setOnScroll((e) -> {
                int page = this.getCurrentPageIndex();
                zoomLevel = Double.min(Double.max(zoomLevel + e.getDeltaY() * zoomSensitivity, 0.5), 4.0);
                pageImages.get(page).setScaleX(zoomLevel);
                pageImages.get(page).setScaleY(zoomLevel);
            });
            // Set zoom to fill panel when switching to new page, and reset pan
            this.currentPageIndexProperty().addListener((observable, oldValue, newValue) -> {
                int page = (int) newValue;
                pageAutoZoom(page);
                pageResetPan(page);
            });
            // Reset zoom and pan when scaling the window
            ChangeListener<Number> resetPageZoomPan = (obs, oldVal, newVal) -> {
                int page = this.getCurrentPageIndex();
                pageAutoZoom(page);
                pageResetPan(page);
            };
            getStage().widthProperty().addListener(resetPageZoomPan);
            getStage().heightProperty().addListener(resetPageZoomPan);
            getStage().maximizedProperty().addListener((e) -> {
                int page = this.getCurrentPageIndex();
                Platform.runLater(() -> pageAutoZoom(page)); // Doesn't always work, idk if there is a more consistent way
                pageResetPan(page);
            });
            // Setup pagination data
            this.setPageCount(workingTest.getTotalPages());
            this.setPageFactory(n -> pageImages.get(n));
            // Auto zoom AFTER view is created
            Platform.runLater(() -> pageAutoZoom(this.getCurrentPageIndex()));
        }
    }

    private void pageAutoZoom(int page) {
        ImageView imgView = pageImages.get(page);
        zoomLevel = Double.min(this.getWidth(), this.getHeight()) / Double.max(imgView.getImage().getWidth(), imgView.getImage().getHeight());
        pageImages.get(page).setScaleX(zoomLevel);
        pageImages.get(page).setScaleY(zoomLevel);
    }

    private void pageResetPan(int page) {
        panX = 0.0;
        panY = 0.0;
        pageImages.get(page).setTranslateX(panX);
        pageImages.get(page).setTranslateY(panY);
    }
}
