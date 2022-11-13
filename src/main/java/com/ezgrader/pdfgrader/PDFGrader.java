package com.ezgrader.pdfgrader;


import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PDFGrader extends Application {

    private static final int MIN_WIDTH = 300;
    private static final int MIN_HEIGHT = 500;

    private static PDFGrader instance;
    public static Test workingTest;
    private static Stage stage;
    private static String cmdLineStartMode;
    private static int cmdLinePageCount = -1;

    @Override
    public void start(Stage primaryStage) throws Exception{
        instance = this;
        stage = primaryStage;
        stage.setTitle("PDF Grader");
        stage.setMinWidth(MIN_WIDTH);
        stage.setMinHeight(MIN_HEIGHT);
        if (cmdLineStartMode == null) {
            SwitchScene("home.fxml", false);
        } else if (cmdLineStartMode.equals("new")) {
            SwitchScene("setup.fxml");
        } else if (cmdLineStartMode.equals("open")) {
            PDFGrader.SwitchScene("grading.fxml", false);
        }
        primaryStage.show();
    }

    public static PDFGrader getInstance() { return instance; }

    public static void main(String[] args) {
        // Process command line arguments, if any
        if (args.length >= 1) {
            if (args[0].equals("-n")) {
                if (args.length >= 3) {
                    File argFile = new File(args[1]);
                    boolean validNumber = true;
                    try {
                        Integer.parseInt(args[2]);
                    } catch (NumberFormatException e) {
                        validNumber = false;
                    }
                    if (argFile.exists() && validNumber) {
                        if (argFile.isDirectory()) {
                            File[] files = argFile.listFiles();
                            List<File> pdfs = new ArrayList<>();
                            for (File file : files) {
                                if (file.getName().contains(".pdf")) {
                                    pdfs.add(file);
                                }
                            }
                            if (pdfs.size() > 0) {
                                workingTest = new Test(MergePDFs(pdfs).toPath());
                                cmdLineStartMode = "new";
                                cmdLinePageCount = Integer.parseInt(args[2]);
                            } else {
                                System.err.println("No PDFs found in directory");
                            }
                        } else if (args[1].contains(".pdf")) {
                            workingTest = new Test(Path.of(args[1]));
                            cmdLineStartMode = "new";
                            cmdLinePageCount = Integer.parseInt(args[2]);
                        }
                    } else {
                        System.err.println("ERROR: File does not exist or is not a pdf, or <pages per test> argument not a number");
                    }
                } else {
                    System.out.println("Usage: pdfgrader -n <pdf file path> <pages per test>");
                }
            } else if (args[0].equals("-o")) {
                if (args.length >= 2) {
                    try {
                        PDFGrader.workingTest = SaveLoad.LoadTest(new File(args[1]));
                        cmdLineStartMode = "open";
                    } catch (IOException e) {
                        System.err.println("ERROR: Could not open " + args[1]);
                    }
                } else {
                    System.out.println("Usage: pdfgrader -o <test json file path>");
                }
            } else {
                System.out.println("Usages:");
                System.out.println("Start:                 pdfgrader");
                System.out.println("Start with new test:   pdfgrader -n <pdf file path> <pages per test>");
                System.out.println("Start with saved test: pdfgrader -o <test json file path>");
            }
        }

        // Launch app
        try {
            launch(args);
        }catch(NullPointerException e)   {
            System.out.println("NullPointerException error has occurred");
        }
    }

    /**
     * Allows a scene with the given root to be fully resizable
     * @param root
     */
    public static void MakeStretchy(GridPane root) {
        ColumnConstraints col = new ColumnConstraints();
        col.setPercentWidth(100);
        root.getColumnConstraints().add(col);
        RowConstraints row = new RowConstraints();
        row.setPercentHeight(100);
        root.getRowConstraints().add(row);
    }

    /**
     * Switches the application stage to display a new scene
     * @param sceneName
     * @param maintainSize
     * @throws IOException
     */
    public static void SwitchScene(String sceneName, boolean maintainSize) throws IOException {
        try {
            AnchorPane newRoot = FXMLLoader.load(Objects.requireNonNull(PDFGrader.class.getResource(sceneName)));
//            MakeStretchy(newRoot);

            if (maintainSize || stage.isMaximized()) {
                // force maintaining window size
                newRoot.setMinSize(stage.getWidth(), stage.getHeight());
                newRoot.setMaxSize(stage.getWidth(), stage.getHeight());
            }
            stage.setScene(new Scene(newRoot));
            // set back
            newRoot.setMinSize(MIN_WIDTH, MIN_HEIGHT);
            newRoot.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        }catch(NullPointerException e){
            System.out.println("NullPointerException error has occured");
        }
    }

    public static void SwitchScene(String sceneName) throws IOException {
        SwitchScene(sceneName, true);
    }

    public static Stage getStage() {
        return stage;
    }

    public static File OpenFileChooser(String title, FileChooser.ExtensionFilter filter) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(filter);
        fileChooser.setTitle(title);
        //Set initial directory to users downloads
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home") + System.getProperty("file.separator")+ "Downloads"));
        List<File> files = fileChooser.showOpenMultipleDialog(PDFGrader.getStage().getScene().getWindow());
        if (files == null) {
            return null;
        } else if (files.size() == 1) {
            return files.get(0);
        } else {
            return MergePDFs(files);
        }
    }

    public static File MergePDFs(List<File> files) {
        PDFMergerUtility ut = new PDFMergerUtility();
        for (File pdf : files) {
            try {
                ut.addSource(pdf);
            } catch (FileNotFoundException e) {
                System.err.println("Couldn't find file to merge, this shouldn't happen.");
            }
        }
        String firstFilePath = files.get(0).getAbsolutePath();
        firstFilePath = firstFilePath.substring(0, firstFilePath.indexOf("."));
        String lastFileName = files.get(files.size()-1).getName();
        lastFileName = lastFileName.substring(0, lastFileName.indexOf("."));
        String savePath = firstFilePath + "-TO-" + lastFileName + "-MERGED.pdf";
        ut.setDestinationFileName(savePath);
        try {
            ut.mergeDocuments(MemoryUsageSetting.setupMainMemoryOnly());
        } catch (IOException e) {
            Toast.Error("Could not merge pdfs");
        }
        return new File(savePath);
    }

    public static void GoToSetup() throws IOException {
        FileChooser.ExtensionFilter pdfFilter = new FileChooser.ExtensionFilter("PDF files (*.pdf)", "*.pdf", "*.PDF");

        File pdf = PDFGrader.OpenFileChooser("Choose PDF", pdfFilter);
        if (pdf != null) {
            PDFGrader.workingTest = new Test(Paths.get(pdf.getPath()));
            PDFGrader.SwitchScene("setup.fxml", false);
        }
    }

    public static void OpenTest() throws IOException {
        FileChooser.ExtensionFilter jsonFilter = new FileChooser.ExtensionFilter("JSON files (*.json)", "*.json", "*.JSON");
        //Set initial directory to users downloads
        File testFile = PDFGrader.OpenFileChooser("Choose Previously Created Test", jsonFilter);
        if (testFile != null) {
            System.out.println(testFile.getAbsolutePath());
            PDFGrader.workingTest = SaveLoad.LoadTest(new File(testFile.getAbsolutePath()));
            PDFGrader.SwitchScene("grading.fxml", false);
        }
    }

    public static void GoToSettings() {

    }

    public static void Exit() {
        stage.close();
    }

    public static void OpenGithub() {
        PDFGrader.getInstance().getHostServices().showDocument("https://github.com/colbehr/PDF-Grader");
    }

    public static void SetWorkingTest(Path path) {
        if (workingTest != null) workingTest.CloseDocument();
        workingTest = new Test(path);
    }

    public static Path GetWorkingTest() {
        return workingTest.getPdfPath();
    }

    public static int getCmdLinePageCount() {
        return cmdLinePageCount;
    }

    public static void showAboutPage(){

        final Stage window = new Stage();
        window.setTitle("About the Project");
        window.initModality(Modality.WINDOW_MODAL);
        window.initOwner(PDFGrader.getStage());
        Scene dialogScene = null;
        window.setAlwaysOnTop(true);
        try {
            dialogScene = new Scene( FXMLLoader.load(Objects.requireNonNull(PDFGrader.class.getResource("about.fxml"))));
        } catch (IOException e) {
            e.printStackTrace();
        }
        window.setScene(dialogScene);
        window.show();
    }

}

