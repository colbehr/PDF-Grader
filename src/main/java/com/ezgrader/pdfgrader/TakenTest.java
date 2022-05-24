package com.ezgrader.pdfgrader;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class TakenTest {
    private class QuestionResult {
        private double pointsGiven;
        private ObservableList<Feedback> feedbacks;

        private QuestionResult(double pointsGiven) {
            this.pointsGiven = pointsGiven;
            feedbacks = FXCollections.observableArrayList();
        }
    }

    private Test test;

    public int total;
    private Map<Question, QuestionResult> questionResultMap;

    public TakenTest(Test test) {
        this.test = test;
        questionResultMap = new HashMap<>();
    }

    public void GradeQuestion(int questionNumber, double pointsGiven) {
        Question question = test.getQuestions().get(questionNumber);
        pointsGiven = Math.min(pointsGiven, question.getPointsPossible()); // ensure only max amount of points can be given
        if (questionResultMap.containsKey(question)) {
            questionResultMap.get(question).pointsGiven = pointsGiven;
        } else {
            questionResultMap.put(question, new QuestionResult(pointsGiven));
        }
    }

    public void AddFeedbackToQuestion(int questionNumber, Feedback feedback) {
        questionResultMap.get(test.getQuestions().get(questionNumber)).feedbacks.add(feedback);
    }

    public double GetTotalScore() {
        double total = 0.0;
        for (QuestionResult result : questionResultMap.values()) {
            total += result.pointsGiven;
        }
        return total;
    }

    public double GetQuestionPointsGiven(Question question) {
        if (questionResultMap.containsKey(question)) {
            return questionResultMap.get(question).pointsGiven;
        } else {
            questionResultMap.put(question, new QuestionResult(0));
            return 0;
        }
    }

    public double GetQuestionPointsGiven(int questionNumber) {
        return GetQuestionPointsGiven(test.getQuestions().get(questionNumber));
    }

    public ObservableList<Feedback> GetQuestionFeedbacks(Question question) {
        if (questionResultMap.containsKey(question)) {
            return questionResultMap.get(question).feedbacks;
        } else {
            QuestionResult questionResult = new QuestionResult(0);
            questionResultMap.put(question, questionResult);
            return questionResult.feedbacks;
        }
    }

    public ObservableList<Feedback> GetQuestionFeedbacks(int questionNumber) {
        return GetQuestionFeedbacks(test.getQuestions().get(questionNumber));
    }

    public double GetTotalPoints() {
        double total = 0;
        Iterator questionMapIterator = questionResultMap.entrySet().iterator();
        while (questionMapIterator.hasNext()) {
            Map.Entry mapElement = (Map.Entry) questionMapIterator.next();
            Question question = (Question) mapElement.getKey();
            total += questionResultMap.get(question).pointsGiven;
        }
        return total;
    }

    public Map<Question, QuestionResult> getQuestionResultMap() {
        return questionResultMap;
    }
}
