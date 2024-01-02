package gui.audioanalyzer;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;

public class MasterTrackListeners {

    private static ChangeListener<Number> getTimeSliderChangeListener(MasterTrack masterTrack){
        return new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue observableValue, Number oldValue, Number newValue) {
                masterTrack.currentTimeLabel.setText(masterTrack.getTime(new Duration(masterTrack.timeSlider.getValue() * masterTrack.MILLISECONDS_PER_SECOND)) + " / ");
            }
        };
    }

    private static EventHandler<MouseEvent> getSwitchButtonOnMouseClickedEH(MasterTrack masterTrack){
        return new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                AudioTrack focusedTrack = masterTrack.getFocusedTrack();
                int focusedTrackNumber = focusedTrack.trackNumber;
                int indexOfNextFocusTrack = focusedTrackNumber;
                if(masterTrack.audioTracks.size() == focusedTrackNumber){
                    masterTrack.audioTracks.get(0).focusTrack();
                }
                else{
                    masterTrack.audioTracks.get(indexOfNextFocusTrack).focusTrack();
                }
            }
        };
    }

    private static EventHandler<MouseEvent> getTimeSliderOnDragDetectedEH(MasterTrack masterTrack){
        return new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(masterTrack.synced){
                    // Mute audio if scrubbing.
                    for(AudioTrack track: masterTrack.audioTracks){
                        if(track.trackHasFile()){
                            track.mediaPlayer.setMute(true);
                            track.isMuted = true;
                        }
                    }
                }
            }
        };
    }

    private static EventHandler<MouseEvent> getTimeSliderOnMouseReleasedEH(MasterTrack masterTrack){
        return new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(masterTrack.synced){
                    // Un-mute audio after scrubbing.
                    for(AudioTrack track: masterTrack.audioTracks){
                        if(track.trackHasFile()){
                            track.mediaPlayer.setMute(false);
                            track.isMuted = false;
                            track.pauseTime = track.mediaPlayer.getCurrentTime().toSeconds(); // Update pause time.
                            // Update time label to fix sluggish time bug.
                        }
                    }
                }
            }
        };
    }

    private static EventHandler<ActionEvent> getSyncButtonOnActionEH(MasterTrack masterTrack){
        return new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if(masterTrack.synced){
                    masterTrack.syncButton.setText("Sync");
                    masterTrack.synced = false;
                    masterTrack.unSync();
                }
                else{
                    masterTrack.syncButton.setText("Unlock");
                    masterTrack.synced = true;
                    masterTrack.sync();
                }

                // Refocus focused track if one exists.
                for(AudioTrack track: masterTrack.audioTracks){
                    if(track.focused){
                        track.focusTrack();
                    }
                }
            }
        };
    }

    private static EventHandler<ActionEvent> getAddTrackButtonOnAction(MasterTrack masterTrack){
        return new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                masterTrack.numberOfAudioTracks++;
                if(masterTrack.numberOfAudioTracks >= masterTrack.MAX_TRACKS){
                    masterTrack.addTrackButton.setDisable(true);
                }
                AudioTrack audioTrack = new AudioTrack(masterTrack.numberOfAudioTracks, new AudioTrackCoordinates(masterTrack.numberOfAudioTracks), masterTrack);
                masterTrack.controller.showAudioTrack(audioTrack);
                masterTrack.audioTracks.add(audioTrack);

                // Disable buttons and slider of new track because it has no file to play.
                if(masterTrack.synced){
                    audioTrack.PPRButton.setDisable(true);
                    audioTrack.timeSlider.setDisable(true);
                    audioTrack.volumeSlider.setDisable(true);
                }

                masterTrack.controller.resizeStageForAudioTrackChange();

                for(Track track: masterTrack.audioTracks){
                    if(track.trackNumber == masterTrack.audioTracks.size()){
                        track.lowerSeparator.setVisible(false);
                    }
                    else{
                        track.lowerSeparator.setVisible(true);
                    }
                }
            }
        };
    }

    // The PPR button will play all tracks from their current position.
    // When not synced, the PPR button should read 'Pause' only if all audio tracks are playing.
    // And it should read 'Play' otherwise.
    // If it reads 'Play' it should force all tracks to play when pressed.
    // If it reads 'Pause' it should force all tracks to pause when pressed.
    private static EventHandler<ActionEvent> getPPRButtonOnActionEH(MasterTrack masterTrack){
        return new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                if(masterTrack.PPRButton.getText().equals("Pause")){
                    for(AudioTrack track: masterTrack.audioTracks){
                        if(track.trackHasFile() && track.isPlaying){
                            // Pause all playing tracks.
                            track.pprOnAction();
                            if(track.atEndOfMedia){
                                track.pprOnAction();
                            }
                        }
                    }
                    masterTrack.PPRButton.setText("Play");
                }
                else if(masterTrack.PPRButton.getText().equals("Play")){
                    for(AudioTrack track: masterTrack.audioTracks){
                        if(track.trackHasFile() && !track.isPlaying){
                            // Play all paused tracks.
                            track.pprOnAction();
                        }
                    }
                    masterTrack.PPRButton.setText("Pause");
                }
                else if(masterTrack.PPRButton.getText().equals("Restart")){
                    // TODO: Fix bug: tracks do not restart when PPRButton reads 'Restart'.
                }
            }
        };
    }

    static void addTimeSliderChangeListener(MasterTrack masterTrack){
        ChangeListener<Number> newChangeListener = getTimeSliderChangeListener(masterTrack);
        masterTrack.timeSliderChangeListener = newChangeListener;
        masterTrack.timeSlider.valueProperty().addListener(newChangeListener);
    }

    static void addSwitchButtonOnMouseClickedEH(MasterTrack masterTrack){
        EventHandler<MouseEvent> newEventHandler = getSwitchButtonOnMouseClickedEH(masterTrack);
        masterTrack.switchButtonOnMouseClickedEH = newEventHandler;
        masterTrack.switchButton.setOnMouseClicked(newEventHandler);
    }

    static void addTimeSliderOnDragDetectedEH(MasterTrack masterTrack){
        EventHandler<MouseEvent> newEventHandler = getTimeSliderOnDragDetectedEH(masterTrack);
        masterTrack.timeSliderOnDragDetectedEH = newEventHandler;
        masterTrack.timeSlider.setOnDragDetected(newEventHandler);
    }

    static void addTimeSliderOnMouseReleasedEH(MasterTrack masterTrack){
        EventHandler<MouseEvent> newEventHandler = getTimeSliderOnMouseReleasedEH(masterTrack);
        masterTrack.timeSliderOnMouseReleasedEH = newEventHandler;
        masterTrack.timeSlider.setOnMouseReleased(newEventHandler);
    }

    static void addSyncButtonOnActionEH(MasterTrack masterTrack){
        EventHandler<ActionEvent> newEventHandler = getSyncButtonOnActionEH(masterTrack);
        masterTrack.syncButtonOnActionEH = newEventHandler;
        masterTrack.syncButton.setOnAction(newEventHandler);
    }

    static void addAddTrackButtonOnAction(MasterTrack masterTrack){
        EventHandler<ActionEvent> newEventHandler = getAddTrackButtonOnAction(masterTrack);
        masterTrack.addTrackButtonOnAction = newEventHandler;
        masterTrack.addTrackButton.setOnAction(newEventHandler);
    }

    static void addPPRButtonOnActionEH(MasterTrack masterTrack){
        EventHandler<ActionEvent> newEventHandler = getPPRButtonOnActionEH(masterTrack);
        masterTrack.pprButtonOnActionEH = newEventHandler;
        masterTrack.PPRButton.setOnAction(newEventHandler);
    }

    /**
     * Binds the valueProperty of two sliders to each other. Binding is unidirectional.
     * @param sliderOne The first slider to bind.
     * @param sliderTwo The second slider to bind.
     */
    public static void bindSliderValueProperties(Slider sliderOne, Slider sliderTwo) {
        // Has to be bidirectional, otherwise the master time slider won't scroll automatically when played.
        sliderTwo.valueProperty().bindBidirectional(sliderOne.valueProperty());
    }

    /**
     * Binds the textProperty of labelOne to labelTwo. Binding is unidirectional.
     * @param labelOne The label whose textProperty will be bound to labelTwo.
     * @param labelTwo The label whose textProperty will not be bound.
     */
    public static void bindLabelTextProperties(Label labelOne, Label labelTwo){
        labelOne.textProperty().bind(labelTwo.textProperty());
    }
}
