package gui.audioanalyzer;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    // Data.
    @FXML
    ScrollPane scrollPane;
    @FXML
    AnchorPane anchorPane;
    private MasterTrack masterTrack; // Private so masterTrack can't access itself through masterTrack.controller.

    /**
     * Called to initialize a controller after its root element has been completely processed.
     * @param url The location used to resolve relative paths for the root object, or null if the location is not known.
     * @param resourceBundle The resources used to localize the root object, or null if the root object was not localized.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Add one master track and audio track at the start.
        addMasterTrack();
        masterTrack.addTrackButton.fire();
    }

    /**
     * Adds JavaFX elements of a Track to anchorPane.
     * @param track The Track to show.
     */
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

    /**
     * Adds JavaFX elements unique to MasterTrack to anchorPane.
     * @param track The MasterTrack to show.
     */
    void showMasterTrack(MasterTrack track){
        showTrack(track);
        ObservableList<Node> anchorPaneChildren = anchorPane.getChildren();
        anchorPaneChildren.add(track.focusTrackLabel);
        anchorPaneChildren.add(track.switchButton);
        anchorPaneChildren.add(track.syncButton);
        anchorPaneChildren.add(track.addTrackButton);
    }

    /**
     * Adds JavaFX elements unique to AudioTrack to anchorPane.
     * @param track The AudioTrack to show.
     */
    void showAudioTrack(AudioTrack track){
        showTrack(track);
        ObservableList<Node> anchorPaneChildren = anchorPane.getChildren();
        anchorPaneChildren.add(track.upperSeparator);
        anchorPaneChildren.add(track.audioLabel);
        anchorPaneChildren.add(track.removeTrackButton);
    }

    /**
     * Removes JavaFX elements of a Track from anchorPane.
     * @param track The Track to remove.
     */
    void removeTrack(Track track){
        ObservableList<Node> anchorPaneChildren = anchorPane.getChildren();
        anchorPaneChildren.remove(track.trackLabel);
        anchorPaneChildren.remove(track.lowerVolumeLabel);
        anchorPaneChildren.remove(track.volumeSlider);
        anchorPaneChildren.remove(track.raiseVolumeLabel);
        anchorPaneChildren.remove(track.PPRButton);
        anchorPaneChildren.remove(track.timeSlider);
        anchorPaneChildren.remove(track.currentTimeLabel);
        anchorPaneChildren.remove(track.totalTimeLabel);
    }

    /**
     * Removes JavaFX elements unique to AudioTrack from anchorPane.
     * @param track The AudioTrack to remove.
     */
    void removeAudioTrack(AudioTrack track){
        removeTrack(track);
        ObservableList<Node> anchorPaneChildren = anchorPane.getChildren();
        anchorPaneChildren.remove(track.upperSeparator);
        anchorPaneChildren.remove(track.audioLabel);
        anchorPaneChildren.remove(track.removeTrackButton);
    }

    /**
     * Adds a new master track to the gui.
     */
    private void addMasterTrack(){
        masterTrack = new MasterTrack(new MasterTrackCoordinates(), this);
        showMasterTrack(masterTrack);
    }

    void resizeStageForNewAudioTrack(){
        // Resize stage.
        Scene scene = anchorPane.getScene();
        if(scene != null){
            Stage stage = (Stage) scene.getWindow();
            if(stage != null){
                double stageHeight = stage.getHeight();
                double newHeight = stageHeight + AudioTrackCoordinates.AUDIO_TRACK_HEIGHT;
                stage.setHeight(newHeight);
                stage.setMaxHeight(newHeight);
            }
        }
    }

    void resizeStageForRemovedAudioTrack(){
        // Resize stage.
        Scene scene = anchorPane.getScene();
        if(scene != null){
            Stage stage = (Stage) scene.getWindow();
            if(stage != null){
                double stageHeight = stage.getHeight();
                double newHeight = stageHeight - AudioTrackCoordinates.AUDIO_TRACK_HEIGHT;
                stage.setHeight(newHeight);
                stage.setMaxHeight(newHeight);
            }
        }
    }
}
