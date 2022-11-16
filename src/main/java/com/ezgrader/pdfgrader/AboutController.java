package com.ezgrader.pdfgrader;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.stage.Stage;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class AboutController {
    private Application app;
    @FXML
    public void initialize() {
        app = new Application() {
            @Override
            public void start(Stage stage) throws Exception {

            }
        };
    }
    public void ColbyGithub() throws URISyntaxException, IOException {
        app.getHostServices().showDocument(new URI("https://www.github.com/colbehr").toString());
    }
    public void ColbyWebsite() throws URISyntaxException, IOException {
        app.getHostServices().showDocument(new URI("https://www.colbehr.com").toString());
    }

    public void KielGithub() throws URISyntaxException, IOException {
        app.getHostServices().showDocument(new URI("https://github.com/kielmorris34").toString());
    }
    public void KielWebsite() throws URISyntaxException, IOException {
        app.getHostServices().showDocument(new URI("https://www.kielmorris.dev").toString());
    }

    public void DaneGithub() throws URISyntaxException, IOException {
        app.getHostServices().showDocument(new URI("https://www.github.com/colbehr").toString());
    }
    public void DaneWebsite() throws URISyntaxException, IOException {
        app.getHostServices().showDocument(new URI("https://www.colbehr.com").toString());
    }

    public void ShadrachGithub() throws URISyntaxException, IOException {
        app.getHostServices().showDocument(new URI("https://www.github.com/colbehr").toString());
    }
    public void ShadrachWebsite() throws URISyntaxException, IOException {
        app.getHostServices().showDocument(new URI("https://www.colbehr.com").toString());
    }

}
