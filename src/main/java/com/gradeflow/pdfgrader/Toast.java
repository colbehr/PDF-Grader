package com.gradeflow.pdfgrader;

import javafx.concurrent.Task;
import javafx.geometry.Bounds;
import javafx.scene.control.Label;
import javafx.scene.text.Text;
import javafx.stage.Popup;

/**
 * Static class for displaying small, temporary messages to the user
 */
public class Toast {
    public static void Notification(String text) {
        MakeToast(text, "-fx-background-color: black; -fx-text-fill: white; -fx-padding: 5px;");
    }

    public static void Error(String text) {
        MakeToast(text, "-fx-background-color: red; -fx-text-fill: black; -fx-padding: 5px;");
    }

    public static void Warning(String text) {
        MakeToast(text, "-fx-background-color: yellow; -fx-text-fill: black; -fx-padding: 5px;");
    }

    /**
     * Make new toast and immediately display to user for 3 seconds
     * @param text
     * @param style
     */
    private static void MakeToast(String text, String style) {
        if (PDFGrader.getStage() == null) return;

        Popup toast = new Popup();
        Label label = new Label(text);
        label.setStyle(style);
        toast.getContent().add(label);
        Bounds rootBounds = PDFGrader.getStage().getScene().getRoot().getLayoutBounds();
        toast.setX(PDFGrader.getStage().getX() + 15);
        toast.setY(PDFGrader.getStage().getY() + rootBounds.getHeight() - 10);
        toast.show(PDFGrader.getStage());
        delay(3000, toast::hide);
    }

    /**
     * Helper method to hide toast after a delay
     * @param millis
     * @param continuation
     */
    private static void delay(long millis, Runnable continuation) {
        Task<Void> sleeper = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try { Thread.sleep(millis); }
                catch (InterruptedException e) { }
                return null;
            }
        };
        sleeper.setOnSucceeded(event -> continuation.run());
        new Thread(sleeper).start();
    }
}
