package sample;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;

public class Feedback {
    private final SimpleStringProperty points;
    private final SimpleStringProperty explanation;

    public Feedback(String points, String explanation) {
        this.points = new SimpleStringProperty(this, "points", points);
        this.explanation = new SimpleStringProperty(this, "explanation", explanation);
    }

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
}
