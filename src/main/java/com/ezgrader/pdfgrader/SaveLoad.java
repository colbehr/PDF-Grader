package com.ezgrader.pdfgrader;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;


public class SaveLoad {
    public static void SaveTest(Test test, String filepath, int currentQuestion, int currentTakenTest) {
        JSONArray wrapperJSArr = new JSONArray();
        JSONObject testJSObj = new JSONObject();
        testJSObj.put("path", test.getPath());
        testJSObj.put("pagesPerTest", test.getPagesPerTest());
        testJSObj.put("name", test.getName());
        // Save where grading left off
        JSONObject savedPlaceJSObj = new JSONObject();
        testJSObj.put("savedPlace", savedPlaceJSObj);
        savedPlaceJSObj.put("question", currentQuestion);
        savedPlaceJSObj.put("takenTest", currentTakenTest);

        wrapperJSArr.put(testJSObj);

        // Store Questions
        JSONArray questionsJSArr = new JSONArray();
        testJSObj.put("questions", questionsJSArr);    // place in hierarchy
        for (Question question: test.getQuestions()) { // fill
            JSONObject questionJSObj = new JSONObject();
            questionJSObj.put("qNum", question.getQNum());
            questionJSObj.put("pointsPossible", question.getPointsPossible());
            questionJSObj.put("pageNum", question.getPageNum());

            questionsJSArr.put(questionJSObj);
        }

        // Store Taken Tests
        JSONArray takenTestsJSArr = new JSONArray();
        testJSObj.put("takenTests", takenTestsJSArr); // place then fill
        for (TakenTest takenTest : test.getTakenTests()) {
            JSONObject takenTestJSObj = new JSONObject();
            takenTestsJSArr.put(takenTestJSObj);
            // For now, nothing is in the takenTest objects except the questions,
            // but I'm leaving room in the hierarchy in case something like the name
            // of the test taker needs to be stored later on

            // Store Question Results
            JSONArray questionResultsJSArr = new JSONArray();
            takenTestJSObj.put("questions", questionResultsJSArr);
            for (Question question : takenTest.getQuestionResultMap().keySet()) {
                // Question Results are within a map in a TakenTest so fetching
                // them is a bit more involved
                JSONObject questionResultJSObj = new JSONObject();
                questionResultsJSArr.put(questionResultJSObj);

                // Question Number
                questionResultJSObj.put("qNum", question.getQNum());
                // Score
                questionResultJSObj.put("pointsGiven", takenTest.GetQuestionPointsGiven(question));
                // Feedbacks
                JSONArray feedbacksJSArr = new JSONArray();
                questionResultJSObj.put("feedbacks", feedbacksJSArr);
                for (Feedback feedback : takenTest.GetQuestionFeedbacks(question)) {
                    JSONObject feedbackJSObj = new JSONObject();
                    feedbackJSObj.put("points", feedback.getPoints());
                    feedbackJSObj.put("explanation", feedback.getExplanation());

                    feedbacksJSArr.put(feedbackJSObj);
                }
            }
        }

        // Write JSON file
        try {
            FileWriter writer = new FileWriter(filepath);
            writer.write(wrapperJSArr.toString());
            writer.close();
        } catch (IOException e) {
            System.out.println("An error occurred while writing file");
        }
    }

    public static Test LoadTest(File testFile) throws IOException {
        String jsonString = Files.readString(testFile.toPath());
        JSONObject testJSObj = (new JSONArray(jsonString)).getJSONObject(0);

        // Create Test
        Path path = Path.of((String) testJSObj.get("path"));
        Test test = new Test(path);
        test.setPagesPerTest(testJSObj.getInt("pagesPerTest"));
        test.setName(testJSObj.getString("name"));
        // Set where grading left off
        JSONObject savedPlaceJSObj = testJSObj.getJSONObject("savedPlace");
        test.setSavedPlace(savedPlaceJSObj.getInt("question"), savedPlaceJSObj.getInt("takenTest"));

        // Retrieve Questions
        for (Object q : testJSObj.getJSONArray("questions")) {
            JSONObject questionJSObj = (JSONObject) q;
            int qNum = questionJSObj.getInt("qNum");
            double pointsPossible = questionJSObj.getDouble("pointsPossible");
            int pageNum = questionJSObj.getInt("pageNum");
            Question question = new Question(qNum, pointsPossible, pageNum);

            test.getQuestions().add(question);
        }

        // Retrieve TakenTests
        test.CreateTakenTests();
        TakenTest takenTests[] = test.getTakenTests();
        JSONArray takenTestsJSArr = testJSObj.getJSONArray("takenTests");
        for (int i = 0; i < takenTestsJSArr.length(); i++) {
            TakenTest takenTest = new TakenTest(test);
            JSONArray takenTestQuestionsJSArr = takenTestsJSArr.getJSONObject(i).getJSONArray("questions");
            // Retrieve Question Results
            for (Object q : takenTestQuestionsJSArr) {
                JSONObject questionJSObj = (JSONObject) q;
                int qNum = questionJSObj.getInt("qNum"); // 1-indexed, adjust to 0-indexed when getting question
                double pointsGiven = questionJSObj.getDouble("pointsGiven");
                takenTest.GradeQuestion(qNum - 1, pointsGiven);

                // Retrieve Feedbacks
                for (Object f : questionJSObj.getJSONArray("feedbacks")) {
                    JSONObject feedbackJSObj = (JSONObject) f;
                    String points = feedbackJSObj.getString("points");
                    String explanation = feedbackJSObj.getString("explanation");
                    takenTest.AddFeedbackToQuestion(qNum - 1, new Feedback(points, explanation));
                }
            }
            takenTests[i] = takenTest;
        }
        return test;
    }
}
