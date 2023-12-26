package gui.audioanalyzer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class AudioAnalyzerApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(AudioAnalyzerApplication.class.getResource("audio_analyzer.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 444, 305); // v1 changed from 405
        stage.setTitle("Audio Analyzer");
        // stage.setResizable(false);
        stage.setScene(scene);
//        MainController controller = (MainController)fxmlLoader.getController();
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}

