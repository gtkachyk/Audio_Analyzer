package gui.audioanalyzer;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
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
    AnchorPane anchorPane;
    static MasterTrack masterTrack;
    static int numberOfAudioTracks = 0;
    static ArrayList<AudioTrack> audioTracks = new ArrayList<>();
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

    void showTrack(Track track){
        ObservableList<Node> anchorPaneChildren = anchorPane.getChildren();
        anchorPaneChildren.add(track.trackLabel);
        anchorPaneChildren.add(track.lowerVolumeLabel);
        anchorPaneChildren.add(track.volumeSlider);
        anchorPaneChildren.add(track.raiseVolumeLabel);
        anchorPaneChildren.add(track.PPRButton);
        anchorPaneChildren.add(track.timeSlider);
        anchorPaneChildren.add(track.currentTimeLabel);
        anchorPaneChildren.add(track.totalTimeLabel);
    }

    void showMasterTrack(MasterTrack track){
        showTrack(track);
        ObservableList<Node> anchorPaneChildren = anchorPane.getChildren();
        anchorPaneChildren.add(track.focusTrackLabel);
        anchorPaneChildren.add(track.switchButton);
        anchorPaneChildren.add(track.syncButton);
        anchorPaneChildren.add(track.addTrackButton);
    }

    void showAudioTrack(AudioTrack track){
        showTrack(track);
        ObservableList<Node> anchorPaneChildren = anchorPane.getChildren();
        anchorPaneChildren.add(track.upperSeparator);
        anchorPaneChildren.add(track.audioLabel);
    }

    /**
     * Adds a new audio track.
     */
    @FXML
    void addTrack(){
        // Resize stage.
//        stage = (Stage) anchorPane.getScene().getWindow();
//        double stageHeight = stage.getHeight();
//        stage.setHeight(stageHeight * 1.5);

        numberOfAudioTracks++;
        AudioTrack audioTrack = new AudioTrack(numberOfAudioTracks, new AudioTrackCoordinates(numberOfAudioTracks));
        showAudioTrack(audioTrack);
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
        masterTrack = new MasterTrack(new MasterTrackCoordinates());
        showMasterTrack(masterTrack);
    }
}
