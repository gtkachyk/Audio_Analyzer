package gui.audioanalyzer;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;

public class AudioAnalyzerApplication extends Application {

    // Constants.
    static final int SCENE_WIDTH = 445;
    static final int SCENE_HEIGHT = 305;
    static final int SCROLL_BAR_PADDING = 25; // Found though trial and error.
    static final int STAGE_MIN_HEIGHT = 234; // Also found through trial and error. Original value: 234.
    static final String APPLICATION_FXML_FILE_NAME = "audio_analyzer.fxml";
    static final String STAGE_TITLE = "Audio Analyzer";

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(AudioAnalyzerApplication.class.getResource(APPLICATION_FXML_FILE_NAME));
        Scene scene = new Scene(fxmlLoader.load(), SCENE_WIDTH, SCENE_HEIGHT);
        stage.setTitle(STAGE_TITLE);
        stage.setResizable(true);
        stage.setMinHeight(STAGE_MIN_HEIGHT);
        stage.setMaxHeight(STAGE_MIN_HEIGHT + AudioTrackCoordinates.AUDIO_TRACK_HEIGHT + 10.0);

        // Make the width of the stage not resizable.
        stage.setMinWidth(SCENE_WIDTH + SCROLL_BAR_PADDING);
        stage.setMaxWidth(SCENE_WIDTH + SCROLL_BAR_PADDING);
        stage.setScene(scene);

        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}

