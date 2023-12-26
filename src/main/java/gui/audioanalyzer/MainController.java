package gui.audioanalyzer;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    // Data.
    @FXML
    private AnchorPane anchorPane;
    static MasterTrack masterTrack;
    private int numberOfAudioTracks = 0;
    public static ArrayList<AudioTrack> audioTracks = new ArrayList<>();
    static AudioTrack longestAudioTrack = null;

    // Methods.
    /**
     * Called to initialize a controller after its root element has been completely processed.
     * @param url The location used to resolve relative paths for the root object, or null if the location is not known.
     * @param resourceBundle The resources used to localize the root object, or null if the root object was not localized.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        addMasterTrack();
        addTrack();
    }

    /**
     *
     */
    @FXML
    private void selectFile(){
        // Open the file browser at the current directory.
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File("src/test_audio_files"));
        fileChooser.setTitle("Select a file for lane " + 1 + "...");
        File selectedFile = fileChooser.showOpenDialog(new Stage());
        if(selectedFile != null){
            // Do something.
        }
    }

    /**
     * Adds a new audio track.
     */
    @FXML
    private void addTrack(){
        // Resize stage.
//        stage = (Stage) anchorPane.getScene().getWindow();
//        double stageHeight = stage.getHeight();
//        stage.setHeight(stageHeight * 1.5);

        numberOfAudioTracks++;
        AudioTrack audioTrack = new AudioTrack(numberOfAudioTracks, new AudioTrackCoordinates(numberOfAudioTracks), anchorPane);
        audioTracks.add(audioTrack);

        // Update the longest track.
        if(longestAudioTrack == null || audioTrack.mediaPlayer.getTotalDuration().toSeconds() > longestAudioTrack.mediaPlayer.getTotalDuration().toSeconds()){
            longestAudioTrack = audioTrack;

            // Update master track length even if not synced.
            masterTrack.bindSliderMaxValueProperties(masterTrack.timeSlider, longestAudioTrack.timeSlider);
            masterTrack.bindLabelValueProperties(masterTrack.totalTimeLabel, longestAudioTrack.totalTimeLabel);
            masterTrack.bindLabelValueProperties(masterTrack.currentTimeLabel, longestAudioTrack.currentTimeLabel);
        }

        // Sync master track with newly added audio track if needed.
        if(masterTrack.synced){
            masterTrack.bindSliderValueProperties(audioTrack.timeSlider, masterTrack.timeSlider);
            masterTrack.bindSliderValueProperties(audioTrack.volumeSlider, masterTrack.volumeSlider);
            masterTrack.bindSliderOnMouseClickedProperty(masterTrack.timeSlider, audioTrack.timeSlider);
            masterTrack.bindSliderOnDragDetectedProperty(masterTrack.timeSlider, audioTrack.timeSlider);
            masterTrack.bindSliderOnMouseReleasedProperty(masterTrack.timeSlider, audioTrack.timeSlider);
            masterTrack.bindButtonTextProperties(masterTrack.PPRButton, audioTrack.PPRButton);
        }
    }

    private void addMasterTrack(){
        masterTrack = new MasterTrack(new MasterTrackCoordinates(), anchorPane);
    }
}
