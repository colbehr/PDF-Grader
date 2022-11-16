package com.ezgrader.pdfgrader;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;


public class SaveLoad {

    public static final String RECENTS_FILENAME = "pdfgrader-recents.txt";
    public static Set<String> recents;

    public static void SaveTest(Test test, String filepath, int currentQuestion, int currentTakenTest) {
        JSONArray wrapperJSArr = new JSONArray();
        JSONObject testJSObj = new JSONObject();
        testJSObj.put("path", test.getPdfPath());
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

            // Store Taken Test ID
            takenTestJSObj.put("id", takenTest.getId());

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

            PutTestInRecent(filepath);
        } catch (IOException e) {
            System.err.println("An error occurred while writing file");
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
        test.savePath = testFile.toPath();

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
            // Retrieve ID
            if (takenTestsJSArr.getJSONObject(i).has("id")) {
                takenTest.setId(takenTestsJSArr.getJSONObject(i).getString("id"));
            } else {
                takenTest.setId("test_" + i);
            }

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

        PutTestInRecent(testFile.getAbsolutePath());

        return test;
    }

    private static void LoadRecentTestsFromFile() throws IOException {
        if (recents == null) {
            recents = new LinkedHashSet<>();
            File recentsFile = new File(RECENTS_FILENAME);
            recentsFile.createNewFile(); // does nothing if already exists
            Scanner scanner = new Scanner(recentsFile);
            while (scanner.hasNextLine()) {
                recents.add(scanner.nextLine());
            }
            scanner.close();
        }
    }

    private static void PutTestInRecent(String absPath) throws IOException {
        LoadRecentTestsFromFile();
        if (recents.contains(absPath)) recents.remove(absPath);
        recents.add(absPath);

        List<String> recentsList = GetRecentTests();
        Collections.reverse(recentsList); // need to reverse back for writing, so order is right in initial set fill
        FileWriter writer = new FileWriter(RECENTS_FILENAME, false);
        for (String testPath : recentsList) {
            writer.write(testPath + "\n");
        }
        writer.close();
    }

    public static List<String> GetRecentTests() throws IOException {
        LoadRecentTestsFromFile();
        List<String> recentsList = new ArrayList<>();
        recentsList.addAll(recents);
        Collections.reverse(recentsList); // sort by most recent first
        System.out.println(recentsList);
        return recentsList;
    }
}
