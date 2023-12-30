package gui.audioanalyzer;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    // Data.
    @FXML
    VBox vBox;
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
        anchorPaneChildren.add(track.lowerSeparator);
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
        anchorPaneChildren.remove(track.lowerSeparator);
    }

    /**
     * Removes JavaFX elements unique to AudioTrack from anchorPane.
     * @param track The AudioTrack to remove.
     */
    void removeAudioTrack(AudioTrack track){
        removeTrack(track);
        ObservableList<Node> anchorPaneChildren = anchorPane.getChildren();
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

    void resizeStageForAudioTrackChange(){
        // Resize stage.
        Scene scene = anchorPane.getScene();
        if(scene != null){
            Stage stage = (Stage) scene.getWindow();
            if(stage != null){
                double newHeight = (masterTrack.numberOfAudioTracks * AudioTrackCoordinates.AUDIO_TRACK_HEIGHT) + TrackCoordinates.MASTER_TRACK_SEPARATOR_Y_COORDINATE + 45.0 + 10.0;
                stage.setHeight(newHeight + AudioAnalyzerApplication.SCROLL_BAR_PADDING);
                stage.setMaxHeight(newHeight + AudioAnalyzerApplication.SCROLL_BAR_PADDING);
            }
        }
    }

    @FXML
    void setDarkMode(){
        BackgroundFill backgroundFill = new BackgroundFill(Color.BLACK, null, null);
        Background background = new Background(backgroundFill);
        scrollPane.setBackground(background);
        anchorPane.setBackground(background);
        setScrollBarStyle(scrollPane, "-fx-background-color: black; -fx-border-color: black");
        setCornerStyle(scrollPane, "-fx-background-color: black; -fx-border-color: black");
    }

    private void setScrollBarStyle(ScrollPane scrollPane, String style){
        for(Node node: scrollPane.lookupAll(".scroll-bar")){
            if(node instanceof ScrollBar){
                ScrollBar scrollBar = (ScrollBar) node;
                System.out.println(scrollBar.toString());
                scrollBar.setStyle(style);
                if(!scrollBar.isVisible()){
                    System.out.println("Found invisible scroll bar");
                }
            }
        }
    }

    private void setCornerStyle(ScrollPane scrollPane, String style){
        for(Node node: scrollPane.lookupAll(".corner")){
            if(node instanceof StackPane){
                StackPane corner = (StackPane) node;
                corner.setStyle(style);
            }
        }
    }
}
