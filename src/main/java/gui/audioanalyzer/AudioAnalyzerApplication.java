package gui.audioanalyzer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class AudioAnalyzerApplication extends Application {
    // Constants.
    static final int SCENE_WIDTH = 445; // Original value was 445.
    static final int SCENE_HEIGHT = 305;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(AudioAnalyzerApplication.class.getResource("audio_analyzer.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), SCENE_WIDTH, SCENE_HEIGHT);
        stage.setTitle("Audio Analyzer");
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}

