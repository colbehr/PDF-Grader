package com.ezgrader.pdfgrader;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Feedback
 * Stores a string with the number of points added or removed and an explanation
 */
public class Feedback {
    private final SimpleStringProperty points;
    private final SimpleStringProperty explanation;


    public Feedback(String points, String explanation) {
        this.points = new SimpleStringProperty(this, "points", points);
        this.explanation = new SimpleStringProperty(this, "explanation", explanation);
    }

    public Feedback copy() {
        return new Feedback(points.get(), explanation.get());
    }

    /*
    * Getters and Setters
    * */

    public String getPoints() {
        return points.get();
    }

    public StringProperty pointsProperty() {
        return points;
    }

    public String getExplanation() {
        return explanation.get();
    }

    public StringProperty explanationProperty() {
        return explanation;
    }

    @Override
    public String toString() {
        return "Feedback{" +
                "points=" + points.get() +
                ", explanation=" + explanation.get() +
                '}';
    }
}
