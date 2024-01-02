package gui.audioanalyzer;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;

public class AudioTrackListeners {

    private static EventHandler<ActionEvent> getPPRButtonOnActionEH(AudioTrack audioTrack){
        return new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                audioTrack.pprOnAction();
            }
        };
    }

    private static ChangeListener<String> getPPRButtonTextPropertyCL(AudioTrack audioTrack){
        return new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String oldValue, String newValue) {
                if(!audioTrack.masterTrack.synced){
                    if(audioTrack.masterTrack.PPRButton.getText().equals("Play")){
                        if(newValue.equals("Pause")){
                            boolean allTracksPlaying = true;
                            for(AudioTrack track: audioTrack.masterTrack.audioTracks){
                                if(!track.PPRButton.getText().equals("Pause")){
                                    allTracksPlaying = false;
                                }
                            }
                            if(allTracksPlaying){
                                audioTrack.masterTrack.PPRButton.setText("Pause");
                            }
                        }
                    }
                    else if(audioTrack.masterTrack.PPRButton.getText().equals("Pause")){
                        if(newValue.equals("Play")){
                            boolean allTracksPaused = true;
                            for(AudioTrack track: audioTrack.masterTrack.audioTracks){
                                if(!track.PPRButton.getText().equals("Play")){
                                    allTracksPaused = false;
                                }
                            }
                            if(allTracksPaused){
                                audioTrack.masterTrack.PPRButton.setText("Play");
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

    private static ChangeListener<Duration> getMediaPlayerTotalDurationCL(AudioTrack audioTrack){
        return new ChangeListener<Duration>() {
            @Override
            public void changed(ObservableValue<? extends Duration> observableValue, Duration oldDuration, Duration newDuration) {
                audioTrack.bindCurrentTimeLabel();
                audioTrack.timeSlider.setMax(newDuration.toSeconds());
                audioTrack.totalTimeLabel.setText(Track.getTime(newDuration));

                // Automatically update the longest track when a new track is added or the file of an existing track changes.
                // Remove audioTrack from masterTrack.audioTracksSortedByDuration if needed.
                boolean trackInSortedList = false;
                int index = 0;
                for(int i = 0; i < audioTrack.masterTrack.audioTracksSortedByDuration.size(); i++){
                    if(audioTrack.masterTrack.audioTracksSortedByDuration.get(i).trackNumber == audioTrack.trackNumber){
                        trackInSortedList = true;
                        index = i;
                    }
                }
                if(trackInSortedList){
                    // If this is reached it means the existing audioFile of this track was replaced with a new one.
                    audioTrack.masterTrack.audioTracksSortedByDuration.remove(index);
                }

                // Add audioTrack from masterTrack.audioTracksSortedByDuration.
                audioTrack.masterTrack.audioTracksSortedByDuration.add(audioTrack);
                audioTrack.masterTrack.refreshLongestAudioTrack();

                // Refresh sync if needed.
                if(audioTrack.masterTrack.synced){
                    audioTrack.masterTrack.refreshSync();
//                    audioTrack.masterTrack.refreshSync();
                }

                audioTrack.masterTrack.refreshFocus();
            }
        };
    }

    private static ChangeListener<Boolean> getTimeSliderValueChangingCL(AudioTrack audioTrack){
        return new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean wasChanging, Boolean isChanging) {
                audioTrack.bindCurrentTimeLabel();
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
                audioTrack.bindCurrentTimeLabel();
                double currentTime = audioTrack.mediaPlayer.getCurrentTime().toSeconds();
                if(Math.abs(currentTime - newValue.doubleValue()) > 0.5){
                    audioTrack.mediaPlayer.seek(Duration.seconds(newValue.doubleValue()));
                }
                audioTrack.labelMatchEndSong(audioTrack.currentTimeLabel.getText(), audioTrack.totalTimeLabel.getText());
            }
        };
    }

    private static ChangeListener<Duration> getMediaPlayerCurrentTimeCL(AudioTrack audioTrack){
        return new ChangeListener<Duration>() {
            @Override
            public void changed(ObservableValue<? extends Duration> observableValue, Duration oldTime, Duration newTime) {
                audioTrack.bindCurrentTimeLabel();
                if(!audioTrack.timeSlider.isValueChanging()){
                    audioTrack.timeSlider.setValue(newTime.toSeconds());
                }
                audioTrack.labelMatchEndSong(audioTrack.currentTimeLabel.getText(), audioTrack.totalTimeLabel.getText());
            }
        };
    }

    private static Runnable getMediaPlayerOnEndOfMediaR(AudioTrack audioTrack){
        return new Runnable() {
            @Override
            public void run() {
                audioTrack.PPRButton.setText("Restart");
//                if(audioTrack.masterTrack.longestAudioTrack.trackNumber == audioTrack.trackNumber){
//                    audioTrack.masterTrack.PPRButton.setText("Restart");
//                }
                audioTrack.atEndOfMedia = true;
                if(!audioTrack.currentTimeLabel.textProperty().equals(audioTrack.totalTimeLabel.textProperty())){
                    audioTrack.currentTimeLabel.textProperty().unbind();
                    audioTrack.currentTimeLabel.setText(Track.getTime(audioTrack.mediaPlayer.getTotalDuration()) + " / ");
                }
            }
        };
    }

    private static EventHandler<MouseEvent> getTimeSliderOnMouseClickedEH(AudioTrack audioTrack){
        return new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                audioTrack.bindCurrentTimeLabel();
                audioTrack.pauseTime = audioTrack.mediaPlayer.getCurrentTime().toSeconds(); // Update pause time.
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
                audioTrack.pauseTime = audioTrack.mediaPlayer.getCurrentTime().toSeconds(); // Update pause time.

                // Update time label to fix sluggish time bug.
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
                audioTrack.updateFile();
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

    private static Runnable getMediaPlayerOnReadyR(AudioTrack audioTrack){
        return new Runnable() {
            @Override
            public void run() {
                if(audioTrack.masterTrack.synced && audioTrack.masterTrack.PPRButton.getText().equals("Pause")){
                    audioTrack.pprOnAction();
                }
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

    static void addMediaPlayerTotalDurationCL(AudioTrack audioTrack){
        ChangeListener<Duration> newChangeListener = getMediaPlayerTotalDurationCL(audioTrack);
        audioTrack.mediaPlayerTotalDurationCL = newChangeListener;
        audioTrack.mediaPlayer.totalDurationProperty().addListener(newChangeListener);
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
        addMediaPlayerTotalDurationCL(audioTrack);
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

    static void removeStableListeners(AudioTrack audioTrack){
        audioTrack.audioLabel.removeEventHandler(MouseEvent.MOUSE_CLICKED, audioTrack.audioLabelOnMouseClickedEH);
        audioTrack.removeTrackButton.removeEventHandler(MouseEvent.MOUSE_CLICKED, audioTrack.removeTrackButtonOnClickEH);
        audioTrack.PPRButton.textProperty().removeListener(audioTrack.pprButtonTextPropertyCL);
    }

    static void removeUnstableListeners(AudioTrack audioTrack){
        audioTrack.PPRButton.removeEventHandler(ActionEvent.ACTION, audioTrack.pprButtonOnActionEH);
        audioTrack.volumeSlider.valueProperty().removeListener(audioTrack.volumeSliderValuePropertyIL);
        if(audioTrack.mediaPlayer != null){
            audioTrack.mediaPlayer.totalDurationProperty().removeListener(audioTrack.mediaPlayerTotalDurationCL);
            audioTrack.mediaPlayer.currentTimeProperty().removeListener(audioTrack.mediaPlayerCurrentTimeCL);
            audioTrack.mediaPlayer.onEndOfMediaProperty().set(null);
            audioTrack.mediaPlayer.onReadyProperty().set(null);
        }
        audioTrack.timeSlider.valueChangingProperty().removeListener(audioTrack.timeSliderValueChangingCL);
        audioTrack.timeSlider.valueProperty().removeListener(audioTrack.timeSliderValueCL);
        audioTrack.timeSlider.removeEventHandler(MouseEvent.MOUSE_CLICKED, audioTrack.timeSliderOnMouseClickedEH);
        audioTrack.timeSlider.removeEventHandler(MouseEvent.MOUSE_RELEASED, audioTrack.timeSliderOnMouseReleasedEH);
        audioTrack.timeSlider.removeEventHandler(MouseEvent.MOUSE_DRAGGED, audioTrack.timeSliderOnDragDetectedEH);
        audioTrack.trackLabel.removeEventHandler(MouseEvent.MOUSE_CLICKED, audioTrack.trackLabelOnMouseClickedEH);
    }
}
