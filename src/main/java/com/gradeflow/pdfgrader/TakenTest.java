package com.gradeflow.pdfgrader;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Stores all data related to an individual test taker's grading and feedback for a certain Test.
 */
public class TakenTest {
    /**
     * Stores score and feedbacks for a particular Test Question.
     */
    private class QuestionResult {
        private double pointsGiven;

        private ObservableList<Feedback> feedbacks;

        private QuestionResult(double pointsGiven) {
            this.pointsGiven = pointsGiven;
            feedbacks = FXCollections.observableArrayList();
        }
    }

    private Test test;
    private Map<Question, QuestionResult> questionResultMap;
    private String id = ""; // Name that will be used to identify this test taker's exported results pdf.
    public TakenTest(Test test) {
        this.test = test;
        questionResultMap = new HashMap<>();
    }

    public TakenTest(Test test, String id) {
        this.test = test;
        questionResultMap = new HashMap<>();
        this.id = id;
    }

    /**
     * Assigns a score for a certain question.
     * @param questionNumber
     * @param pointsGiven
     */
    public void GradeQuestion(int questionNumber, double pointsGiven) {
        Question question = test.getQuestions().get(questionNumber);
        pointsGiven = Math.min(pointsGiven, question.getPointsPossible()); // ensure only max amount of points can be given
        if (questionResultMap.containsKey(question)) {
            questionResultMap.get(question).pointsGiven = pointsGiven;
        } else {
            questionResultMap.put(question, new QuestionResult(pointsGiven));
        }
    }

    /**
     * Adds a new feedback for the given question.
     * @param questionNumber
     * @param feedback
     */
    public void AddFeedbackToQuestion(int questionNumber, Feedback feedback) {
        questionResultMap.get(test.getQuestions().get(questionNumber)).feedbacks.add(feedback);
    }

    /**
     * Gets the graded score for the given question
     * @param question
     * @return question score
     */
    public double GetQuestionPointsGiven(Question question) {
        if (questionResultMap.containsKey(question)) {
            return questionResultMap.get(question).pointsGiven;
        } else {
            questionResultMap.put(question, new QuestionResult(0));
            return 0;
        }
    }

    /**
     * Gets a question's score by question index
     * @param questionNumber
     * @return question score
     */
    public double GetQuestionPointsGiven(int questionNumber) {
        return GetQuestionPointsGiven(test.getQuestions().get(questionNumber));
    }

    /**
     * Gets all feedbacks for a given question
     * @param question
     * @return List of feedbacks
     */
    public ObservableList<Feedback> GetQuestionFeedbacks(Question question) {
        if (questionResultMap.containsKey(question)) {
            return questionResultMap.get(question).feedbacks;
        } else {
            QuestionResult questionResult = new QuestionResult(0);
            questionResultMap.put(question, questionResult);
            return questionResult.feedbacks;
        }
    }

    /**
     * Gets all feedbacks for a given question index
     * @param questionNumber
     * @return list of Feedbacks
     */
    public ObservableList<Feedback> GetQuestionFeedbacks(int questionNumber) {
        return GetQuestionFeedbacks(test.getQuestions().get(questionNumber));
    }

    /**
     * Gets the total given points for the whole test
     * @return overall score for this TakenTest
     */
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

    /**
     * Gets the number of points that would be a perfect score for this test.
     * @return max score for this Test
     */
    public double GetTotalPointsPossible() {
        double total = 0.0;
        for (Question q : questionResultMap.keySet()) {
            total += q.getPointsPossible();
        }
        return total;
    }

    public Map<Question, QuestionResult> getQuestionResultMap() {
        return questionResultMap;
    }

    public Test getTest() {
        return test;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}
