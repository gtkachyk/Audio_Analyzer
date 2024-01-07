package gui.audioanalyzer;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.util.Optional;
import java.util.concurrent.Callable;

public class AudioTrackListeners {

    private static EventHandler<ActionEvent> getPPRButtonOnActionEH(AudioTrack audioTrack){
        return new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                if(!TrackUtilities.trackHasFile(audioTrack)) return;
                bindCurrentTimeLabel(audioTrack);
                if(audioTrack.atEndOfMedia){
                    audioTrack.restartTrack();
                    return;
                }
                if(audioTrack.isPlaying){
                    audioTrack.pauseTrack();
                }
                else{
                    audioTrack.playTrack();
                }
            }
        };
    }

    private static ChangeListener<String> getPPRButtonTextPropertyCL(AudioTrack audioTrack){
        return new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String oldValue, String newValue) {
                // This is used to update the master PPR text when the tracks aren't synced.
                if(!audioTrack.masterTrack.synced){
                    TrackUtilities.refreshMasterPPRText(audioTrack.masterTrack);
                }
                else{
                    if(TrackUtilities.trackEquals(audioTrack, audioTrack.masterTrack.shortestAudioTrack)){
                        audioTrack.masterTrack.PPRButton.setText(newValue);
                    }
                    for(AudioTrack track: audioTrack.masterTrack.audioTracks){
                        if(TrackUtilities.trackHasFile(track) && !TrackUtilities.trackEquals(track, audioTrack.masterTrack.shortestAudioTrack)){
                            track.PPRButton.setText(newValue);
                            if(newValue.equals("Pause") && !track.mediaPlayer.getStatus().equals(MediaPlayer.Status.PLAYING)){
                                track.mediaPlayer.play();
                                track.isPlaying = true;
                            }
                            else if(newValue.equals("Play") && !track.mediaPlayer.getStatus().equals(MediaPlayer.Status.PAUSED)){
                                track.mediaPlayer.pause();
                                track.isPlaying = false;
                            }
                            else if(newValue.equals("Restart") && !track.mediaPlayer.getStatus().equals(MediaPlayer.Status.PAUSED)){
                                track.mediaPlayer.pause();
                                track.isPlaying = false;
                            }
                        }
                    }
                }
            }
        };
    }

    private static InvalidationListener getVolumeSliderValuePropertyIL(AudioTrack audioTrack){
        return new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                audioTrack.mediaPlayer.setVolume(audioTrack.volumeSlider.getValue());
                if(audioTrack.mediaPlayer.getVolume() != 0.0){
                    audioTrack.isMuted = false;
                }
                else{
                    audioTrack.isMuted = true;
                }
            }
        };
    }

    private static ChangeListener<Boolean> getTimeSliderValueChangingCL(AudioTrack audioTrack){
        return new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean wasChanging, Boolean isChanging) {
                if(!isChanging){
                    audioTrack.mediaPlayer.seek(Duration.seconds(audioTrack.timeSlider.getValue()));
                }
            }
        };
    }

    private static ChangeListener<Number> getTimeSliderValueCL(AudioTrack audioTrack){
        return new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) {
                bindCurrentTimeLabel(audioTrack);
                double currentTime = audioTrack.mediaPlayer.getCurrentTime().toSeconds();
                if(Math.abs(currentTime - newValue.doubleValue()) > 0.5){
                    audioTrack.mediaPlayer.seek(Duration.seconds(newValue.doubleValue()));
                }
                TrackUtilities.compareTimeLabels(audioTrack);
            }
        };
    }

    private static ChangeListener<Duration> getMediaPlayerCurrentTimeCL(AudioTrack audioTrack){
        return new ChangeListener<Duration>() {
            @Override
            public void changed(ObservableValue<? extends Duration> observableValue, Duration oldTime, Duration newTime) {
                bindCurrentTimeLabel(audioTrack);
                if(!audioTrack.timeSlider.isValueChanging()){
                    if(audioTrack.timeSlider.getValue() != audioTrack.timeSlider.getMax()){ // This is needed to address a bug that likely involves the pauseTime mechanic.
                        audioTrack.timeSlider.setValue(newTime.toSeconds());
                    }
                }
                TrackUtilities.compareTimeLabels(audioTrack);
            }
        };
    }

    private static Runnable getMediaPlayerOnEndOfMediaR(AudioTrack audioTrack){
        return new Runnable() {
            @Override
            public void run() {
                bindCurrentTimeLabel(audioTrack);
                audioTrack.PPRButton.setText("Restart");
                audioTrack.atEndOfMedia = true;
            }
        };
    }

    private static EventHandler<MouseEvent> getTimeSliderOnMouseClickedEH(AudioTrack audioTrack){
        return new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                bindCurrentTimeLabel(audioTrack);
                audioTrack.pauseTime = audioTrack.mediaPlayer.getCurrentTime().toSeconds();
            }
        };
    }

    private static EventHandler<MouseEvent> getTimeSliderOnMouseReleasedEH(AudioTrack audioTrack){
        return new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                // Un-mute audio after scrubbing.
                audioTrack.mediaPlayer.setMute(false);
                audioTrack.isMuted = false;
                audioTrack.pauseTime = audioTrack.mediaPlayer.getCurrentTime().toSeconds();
            }
        };
    }

    private static EventHandler<MouseEvent> getTimeSliderOnDragDetectedEH(AudioTrack audioTrack){
        return new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                // Mute audio if scrubbing.
                audioTrack.mediaPlayer.setMute(true);
                audioTrack.isMuted = true;
            }
        };
    }

    private static EventHandler<MouseEvent> getAudioLabelOnMouseClickedEH(AudioTrack audioTrack){
        return new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                audioTrack.getNewAudioFile();
            }
        };
    }

    private static EventHandler<MouseEvent> getTrackLabelOnMouseClickedEH(AudioTrack audioTrack){
        return new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(audioTrack.audioFile != null){
                    if(audioTrack.focused){
                        audioTrack.undoFocus();
                    }
                    else{
                        audioTrack.focusTrack();
                    }
                }
            }
        };
    }

    private static EventHandler<MouseEvent> getRemoveTrackButtonOnClickEH(AudioTrack audioTrack){
        return new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                audioTrack.masterTrack.removeAudioTrack(audioTrack);
            }
        };
    }

    /**
     * This change listener is responsible for AudioTrack setup after a new file is selected.
     * @param audioTrack
     * @return
     */
    private static Runnable getMediaPlayerOnReadyR(AudioTrack audioTrack){
        return new Runnable() {
            @Override
            public void run() {
                // Update GUI properties.
                audioTrack.audioLabel.setText(audioTrack.audioFile.getName());
                audioTrack.timeSlider.setMax(audioTrack.mediaPlayer.getTotalDuration().toSeconds());
                audioTrack.totalTimeLabel.setText(TrackUtilities.getTime(audioTrack.mediaPlayer.getTotalDuration()));
                bindCurrentTimeLabel(audioTrack);

                // Automatically update the shortest track when a new track is added or the file of an existing track changes.
                // Remove audioTrack from masterTrack.audioTracksSortedByDuration if needed.
                if(!TrackUtilities.removeTrackByNumber(audioTrack.masterTrack.audioTracksSortedByDuration, audioTrack.trackNumber)){
                    Alert acknowledgement = Track.controller.createAlert(new Alert(Alert.AlertType.ERROR, "An error occurred in AudioTrackListeners.getMediaPlayerOnReadyR().", ButtonType.OK), "Error");
                    Optional<ButtonType> result = acknowledgement.showAndWait();
                    if(result.isPresent() && result.get() == ButtonType.OK) System.exit(1);
                }
                audioTrack.masterTrack.audioTracksSortedByDuration.add(audioTrack);
                TrackUtilities.refreshShortestAudioTrack(audioTrack.masterTrack);

                // Refresh sync if needed.
                if(audioTrack.masterTrack.synced) audioTrack.masterTrack.refreshSync();
            }
        };
    }

    static void addPPRButtonOnActionEH(AudioTrack audioTrack){
        EventHandler<ActionEvent> newEventHandler = getPPRButtonOnActionEH(audioTrack);
        audioTrack.pprButtonOnActionEH = newEventHandler;
        audioTrack.PPRButton.onActionProperty().set(newEventHandler);
    }

    static void addPPRButtonTextPropertyCL(AudioTrack audioTrack){
        ChangeListener<String> newChangeListener = getPPRButtonTextPropertyCL(audioTrack);
        audioTrack.pprButtonTextPropertyCL = newChangeListener;
        audioTrack.PPRButton.textProperty().addListener(newChangeListener);
    }

    static void addVolumeSliderValuePropertyIL(AudioTrack audioTrack){
        InvalidationListener newInvalidationListener = getVolumeSliderValuePropertyIL(audioTrack);
        audioTrack.volumeSliderValuePropertyIL = newInvalidationListener;
        audioTrack.volumeSlider.valueProperty().addListener(newInvalidationListener);
    }

    static void addTimeSliderValueChangingCL(AudioTrack audioTrack){
        ChangeListener<Boolean> newChangeListener = getTimeSliderValueChangingCL(audioTrack);
        audioTrack.timeSliderValueChangingCL = newChangeListener;
        audioTrack.timeSlider.valueChangingProperty().addListener(newChangeListener);
    }

    static void addTimeSliderValueCL(AudioTrack audioTrack){
        ChangeListener<Number> newChangeListener = getTimeSliderValueCL(audioTrack);
        audioTrack.timeSliderValueCL = newChangeListener;
        audioTrack.timeSlider.valueProperty().addListener(newChangeListener);
    }

    static void addMediaPlayerCurrentTimeCL(AudioTrack audioTrack){
        ChangeListener<Duration> newChangeListener = getMediaPlayerCurrentTimeCL(audioTrack);
        audioTrack.mediaPlayerCurrentTimeCL = newChangeListener;
        audioTrack.mediaPlayer.currentTimeProperty().addListener(newChangeListener);
    }

    static void addMediaPlayerOnEndOfMediaR(AudioTrack audioTrack){
        Runnable newRunnable = getMediaPlayerOnEndOfMediaR(audioTrack);
        audioTrack.mediaPlayerOnEndOfMediaR = newRunnable;
        audioTrack.mediaPlayer.setOnEndOfMedia(newRunnable);
    }

    static void addTimeSliderOnMouseClickedEH(AudioTrack audioTrack){
        EventHandler<MouseEvent> newEventHandler = getTimeSliderOnMouseClickedEH(audioTrack);
        audioTrack.timeSliderOnMouseClickedEH = newEventHandler;
        audioTrack.timeSlider.setOnMouseClicked(newEventHandler);
    }

    static void addTimeSliderOnMouseReleasedEH(AudioTrack audioTrack){
        EventHandler<MouseEvent> newEventHandler = getTimeSliderOnMouseReleasedEH(audioTrack);
        audioTrack.timeSliderOnMouseReleasedEH = newEventHandler;
        audioTrack.timeSlider.setOnMouseReleased(newEventHandler);
    }

    static void addTimeSliderOnDragDetectedEH(AudioTrack audioTrack){
        EventHandler<MouseEvent> newEventHandler = getTimeSliderOnDragDetectedEH(audioTrack);
        audioTrack.timeSliderOnDragDetectedEH = newEventHandler;
        audioTrack.timeSlider.setOnDragDetected(newEventHandler);
    }

    static void addAudioLabelOnMouseClickedEH(AudioTrack audioTrack){
        EventHandler<MouseEvent> newEventHandler = getAudioLabelOnMouseClickedEH(audioTrack);
        audioTrack.audioLabelOnMouseClickedEH = newEventHandler;
        audioTrack.audioLabel.setOnMouseClicked(newEventHandler);
    }

    static void addTrackLabelOnMouseClickedEH(AudioTrack audioTrack){
        EventHandler<MouseEvent> newEventHandler = getTrackLabelOnMouseClickedEH(audioTrack);
        audioTrack.trackLabelOnMouseClickedEH = newEventHandler;
        audioTrack.trackLabel.setOnMouseClicked(newEventHandler);
    }

    static void addRemoveTrackButtonOnClickEH(AudioTrack audioTrack){
        EventHandler<MouseEvent> newEventHandler = getRemoveTrackButtonOnClickEH(audioTrack);
        audioTrack.removeTrackButtonOnClickEH = newEventHandler;
        audioTrack.removeTrackButton.setOnMouseClicked(newEventHandler);
    }

    static void addMediaPlayerOnReadyR(AudioTrack audioTrack){
        Runnable newRunnable = getMediaPlayerOnReadyR(audioTrack);
        audioTrack.mediaPlayerOnReadyR = newRunnable;
        if(audioTrack.mediaPlayer != null){
            audioTrack.mediaPlayer.setOnReady(newRunnable);
        }
    }

    static void removePPRButtonTextPropertyCL(AudioTrack audioTrack){
        if(audioTrack.pprButtonTextPropertyCL != null) audioTrack.PPRButton.textProperty().removeListener(audioTrack.pprButtonTextPropertyCL);
    }

    /**
     * Stable listeners are those that are not connected to the parts of an audio track that are likely to change.
     * @param audioTrack
     */
    static void addStableListeners(AudioTrack audioTrack){
        addAudioLabelOnMouseClickedEH(audioTrack);
        addRemoveTrackButtonOnClickEH(audioTrack);
        addPPRButtonTextPropertyCL(audioTrack);
    }
    
    static void addUnstableListeners(AudioTrack audioTrack){
        addPPRButtonOnActionEH(audioTrack);
        addVolumeSliderValuePropertyIL(audioTrack);
        addMediaPlayerCurrentTimeCL(audioTrack);
        addMediaPlayerOnEndOfMediaR(audioTrack);
        addMediaPlayerOnReadyR(audioTrack);
        addTimeSliderValueChangingCL(audioTrack);
        addTimeSliderValueCL(audioTrack);
        addTimeSliderOnMouseClickedEH(audioTrack);
        addTimeSliderOnMouseReleasedEH(audioTrack);
        addTimeSliderOnDragDetectedEH(audioTrack);
        addTrackLabelOnMouseClickedEH(audioTrack);
    }

    static void addAllListeners(AudioTrack audioTrack){
        addStableListeners(audioTrack);
        addUnstableListeners(audioTrack);
    }

    static void removeStableListeners(AudioTrack audioTrack){
        if(audioTrack.audioLabelOnMouseClickedEH != null) audioTrack.audioLabel.removeEventHandler(MouseEvent.MOUSE_CLICKED, audioTrack.audioLabelOnMouseClickedEH);
        if(audioTrack.removeTrackButtonOnClickEH != null) audioTrack.removeTrackButton.removeEventHandler(MouseEvent.MOUSE_CLICKED, audioTrack.removeTrackButtonOnClickEH);
        if(audioTrack.pprButtonTextPropertyCL != null) audioTrack.PPRButton.textProperty().removeListener(audioTrack.pprButtonTextPropertyCL);
    }

    static void removeUnstableListeners(AudioTrack audioTrack){
        if(audioTrack.pprButtonOnActionEH != null) audioTrack.PPRButton.removeEventHandler(ActionEvent.ACTION, audioTrack.pprButtonOnActionEH);
        if(audioTrack.volumeSliderValuePropertyIL != null) audioTrack.volumeSlider.valueProperty().removeListener(audioTrack.volumeSliderValuePropertyIL);
        if(audioTrack.mediaPlayer != null){
            if(audioTrack.mediaPlayerCurrentTimeCL != null) audioTrack.mediaPlayer.currentTimeProperty().removeListener(audioTrack.mediaPlayerCurrentTimeCL);
            audioTrack.mediaPlayer.onEndOfMediaProperty().set(null); audioTrack.mediaPlayerOnEndOfMediaR = null;
            audioTrack.mediaPlayer.onReadyProperty().set(null); audioTrack.mediaPlayerOnReadyR = null;
        }
        if(audioTrack.timeSliderValueChangingCL != null) audioTrack.timeSlider.valueChangingProperty().removeListener(audioTrack.timeSliderValueChangingCL);
        if(audioTrack.timeSliderValueCL != null) audioTrack.timeSlider.valueProperty().removeListener(audioTrack.timeSliderValueCL);
        if(audioTrack.timeSliderOnMouseClickedEH != null) audioTrack.timeSlider.removeEventHandler(MouseEvent.MOUSE_CLICKED, audioTrack.timeSliderOnMouseClickedEH);
        if(audioTrack.timeSliderOnMouseReleasedEH != null) audioTrack.timeSlider.removeEventHandler(MouseEvent.MOUSE_RELEASED, audioTrack.timeSliderOnMouseReleasedEH);
        if(audioTrack.timeSliderOnDragDetectedEH != null) audioTrack.timeSlider.removeEventHandler(MouseEvent.MOUSE_DRAGGED, audioTrack.timeSliderOnDragDetectedEH);
        if(audioTrack.trackLabelOnMouseClickedEH != null) audioTrack.trackLabel.removeEventHandler(MouseEvent.MOUSE_CLICKED, audioTrack.trackLabelOnMouseClickedEH);
    }

    static void removeAllListeners(AudioTrack audioTrack){
        removeStableListeners(audioTrack);
        removeUnstableListeners(audioTrack);
    }

    static void refreshListeners(AudioTrack audioTrack){
        removeAllListeners(audioTrack);
        addAllListeners(audioTrack);
    }

    static void bindCurrentTimeLabel(AudioTrack audioTrack){
        audioTrack.currentTimeLabel.textProperty().bind(Bindings.createStringBinding(new Callable<String>() {
            @Override
            public String call() {
                // Check where the time slider is.
                if(audioTrack.timeSlider.getValue() == audioTrack.timeSlider.getMax()){
                    if(audioTrack.masterTrack.synced){
                        return TrackUtilities.getTime(audioTrack.masterTrack.shortestAudioTrack.mediaPlayer.getTotalDuration()) + " / ";
                    }
                    else{
                        return TrackUtilities.getTime(audioTrack.mediaPlayer.getTotalDuration()) + " / ";
                    }
                }
                else if(audioTrack.timeSlider.getValue() == 0.0){
                    return TrackUtilities.getTime(audioTrack.mediaPlayer.getStartTime()) + " / ";
                }
                else{
                    return TrackUtilities.getTime(audioTrack.mediaPlayer.getCurrentTime()) + " / ";
                }
            }
        }, audioTrack.mediaPlayer.currentTimeProperty()));
    }
}
