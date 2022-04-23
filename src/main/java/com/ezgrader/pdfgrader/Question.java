package com.ezgrader.pdfgrader;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;

public class Question {

    private final SimpleIntegerProperty qNum;
    private final SimpleDoubleProperty pointsPossible;
    private final SimpleDoubleProperty pointsGiven;
    private final SimpleIntegerProperty pageNum;      // placeholder for question boxes
    //private ObservableSet<Feedback> feedbacks;

    public Question(int qNumber, double pointsPossible, int pageNum) {
        this.qNum = new SimpleIntegerProperty(this, "qNumber", qNumber);
        this.pointsPossible = new SimpleDoubleProperty(this, "pointsPossible", pointsPossible);
        this.pointsGiven = new SimpleDoubleProperty(this, "pointsGiven", 0.0);
        this.pageNum = new SimpleIntegerProperty(this, "pageNum", pageNum);

        // TODO: make a set of reusable question feedbacks
        //this.feedbacks = FXCollections.observableSet();
    }

    public int getQNum() {
        return qNum.get();
    }

    public SimpleIntegerProperty qNumProperty() {
        return qNum;
    }

    public void setQNum(int qNum) {
        this.qNum.set(qNum);
    }

    public double getPointsPossible() {
        return pointsPossible.get();
    }

    public SimpleDoubleProperty pointsPossibleProperty() {
        return pointsPossible;
    }

    public void setPointsPossible(double pointsPossible) {
        this.pointsPossible.set(pointsPossible);
    }

    public double getPointsGiven() {
        return pointsGiven.get();
    }

    public SimpleDoubleProperty pointsGivenProperty() {
        return pointsGiven;
    }

    public void setPointsGiven(double pointsGiven) {
        this.pointsGiven.set(pointsGiven);
    }

    public int getPageNum() {
        return pageNum.get();
    }

    public SimpleIntegerProperty pageNumProperty() {
        return pageNum;
    }

    public void setPageNum(int pageNum) {
        this.pageNum.set(pageNum);
    }

//    public ObservableList<Feedback> getFeedbacks() {
//        return feedbacks;
//    }
}
