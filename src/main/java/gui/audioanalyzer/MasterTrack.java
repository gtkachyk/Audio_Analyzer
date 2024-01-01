package gui.audioanalyzer;

import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import java.util.ArrayList;

public class MasterTrack extends Track{

    // Constants.
    private static final double ADD_TRACK_BUTTON_WIDTH = 69.6;
    private static final double ADD_TRACK_BUTTON_HEIGHT = 25.6;
    private static final double MILLISECONDS_PER_SECOND = 1000.0;
    private static final int MAX_TRACKS = 10;
    private static final double TIME_SLIDER_DEFAULT_MAX = 100.0;

    // JavaFX objects.
    @FXML
    Label focusTrackLabel;
    @FXML
    Button switchButton;
    @FXML
    Button syncButton;
    @FXML
    Button addTrackButton;

    // Other data.
    boolean synced = false;
    ArrayList<AudioTrack> audioTracks = new ArrayList<>(); // Track #n = audioTracks.get(n - 1).
    ArrayList<AudioTrack> audioTracksSortedByDuration = new ArrayList<>(); // Sorted by increasing order of MediaPlayer duration.
    AudioTrack longestAudioTrack = null;
    int numberOfAudioTracks = 0;

    private final ChangeListener<Number> timeSliderChangeListener = new ChangeListener<Number>() {
        @Override
        public void changed(ObservableValue observableValue, Number oldValue, Number newValue) {
            currentTimeLabel.setText(getTime(new Duration(timeSlider.getValue() * MILLISECONDS_PER_SECOND)) + " / ");
        }
    };

    private final EventHandler<MouseEvent> switchButtonOnMouseClickedEH = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
            String currentFocusTrack = getFocusTrack();
            if(currentFocusTrack.equals("None")){
                for(AudioTrack track: audioTracks){
                    if(track.trackNumber == 1){
                        track.volumeSlider.setValue(1.0);
                        Bindings.unbindBidirectional(volumeSlider.valueProperty(), track.volumeSlider.valueProperty());
                    }
                }
            }
        }
    };

    private final EventHandler<MouseEvent> timeSliderOnDragDetectedEH = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
            if(synced){
                // Mute audio if scrubbing.
                for(AudioTrack track: audioTracks){
                    if(track.trackHasFile()){
                        track.mediaPlayer.setMute(true);
                        track.isMuted = true;
                    }
                }
            }
        }
    };

    private final EventHandler<MouseEvent> timeSliderOnMouseReleasedEH = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
            if(synced){
                // Un-mute audio after scrubbing.
                for(AudioTrack track: audioTracks){
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

    public MasterTrack(MasterTrackCoordinates masterTrackCoordinates, MainController controller){
        Track.controller = controller;
        trackNumber = 0;
        trackCoordinates = masterTrackCoordinates;

        trackLabel = new Label("Master");
        initializeTrackObject(trackLabel, getTrackCoordinates().trackLabelX, getTrackCoordinates().trackLabelY, TRACK_LABEL_WIDTH, LABEL_HEIGHT);

        focusTrackLabel = new Label("Focus Track: " + getFocusTrack());
        initializeTrackObject(focusTrackLabel, getTrackCoordinates().focusTrackLabelX, getTrackCoordinates().focusTrackLabelY, AUDIO_LABEL_WIDTH, LABEL_HEIGHT);

        lowerVolumeLabel = new Label("-");
        initializeTrackObject(lowerVolumeLabel, getTrackCoordinates().lowerVolumeLabelX, getTrackCoordinates().lowerVolumeLabelY, LOWER_VOLUME_LABEL_WIDTH, LABEL_HEIGHT);

        volumeSlider = new Slider();
        initializeTrackObject(volumeSlider, getTrackCoordinates().volumeSliderX, getTrackCoordinates().volumeSliderY, VOLUME_SLIDER_WIDTH, SLIDER_HEIGHT);
        volumeSlider.setMax(VOLUME_SLIDER_MAX);
        volumeSlider.setValue(volumeSlider.getMax());

        raiseVolumeLabel = new Label("+");
        initializeTrackObject(raiseVolumeLabel, getTrackCoordinates().raiseVolumeLabelX, getTrackCoordinates().raiseVolumeLabelY, RAISE_VOLUME_LABEL_WIDTH, LABEL_HEIGHT);

        PPRButton = new Button();
        initializeTrackObject(PPRButton, getTrackCoordinates().PPRButtonX, getTrackCoordinates().PPRButtonY, PPR_BUTTON_WIDTH, PPR_BUTTON_HEIGHT);
        PPRButton.setDisable(true);

        timeSlider = new Slider();
        initializeTrackObject(timeSlider, getTrackCoordinates().timeSliderX, getTrackCoordinates().timeSliderY, TIME_SLIDER_WIDTH, SLIDER_HEIGHT);
        timeSlider.setDisable(true);

        currentTimeLabel = new Label("00:00 / ");
        initializeTrackObject(currentTimeLabel, getTrackCoordinates().currentTimeLabelX, getTrackCoordinates().currentTimeLabelY, CURRENT_TIME_LABEL_WIDTH, LABEL_HEIGHT);

        totalTimeLabel = new Label("00:00");
        initializeTrackObject(totalTimeLabel, getTrackCoordinates().totalTimeLabelX, getTrackCoordinates().totalTimeLabelY, TOTAL_TIME_LABEL_WIDTH, LABEL_HEIGHT);

        switchButton = new Button("Focus");
        initializeTrackObject(switchButton, getTrackCoordinates().switchButtonX, getTrackCoordinates().switchButtonY, PPR_BUTTON_WIDTH, PPR_BUTTON_HEIGHT);

        syncButton = new Button("Sync");
        initializeTrackObject(syncButton, getTrackCoordinates().syncButtonX, getTrackCoordinates().syncButtonY, PPR_BUTTON_WIDTH, PPR_BUTTON_HEIGHT);
        syncButton.setDisable(true);

        addTrackButton = new Button("Add Track");
        initializeTrackObject(addTrackButton, getTrackCoordinates().addTrackButtonX, getTrackCoordinates().addTrackButtonY, ADD_TRACK_BUTTON_WIDTH, ADD_TRACK_BUTTON_HEIGHT);

        lowerSeparator = new Separator();
        initializeTrackObject(lowerSeparator, 0.0, MasterTrackCoordinates.MASTER_TRACK_SEPARATOR_Y_COORDINATE, SEPARATOR_WIDTH, SEPARATOR_HEIGHT);

        initializeTrack();
    }

    @Override
    void initializeTrack() {
        PPRButton.setText("Play");

        // Add listeners.
        syncButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if(synced){
                    syncButton.setText("Sync");
                    synced = false;
                    unSync();
                }
                else{
                    syncButton.setText("Unlock");
                    synced = true;
                    sync();
                }

                // Refocus focused track if one exists.
                for(AudioTrack track: audioTracks){
                    if(track.focused){
                        track.focusTrack();
                    }
                }
            }
        });

        addTrackButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                numberOfAudioTracks++;
                if(numberOfAudioTracks >= MAX_TRACKS){
                    addTrackButton.setDisable(true);
                }
                AudioTrack audioTrack = new AudioTrack(numberOfAudioTracks, new AudioTrackCoordinates(numberOfAudioTracks), MasterTrack.this);
                controller.showAudioTrack(audioTrack);
                audioTracks.add(audioTrack);

                // Disable buttons and slider of new track because it has no file to play.
                if(synced){
                    audioTrack.PPRButton.setDisable(true);
                    audioTrack.timeSlider.setDisable(true);
                    audioTrack.volumeSlider.setDisable(true);
                }

                controller.resizeStageForAudioTrackChange();

                for(Track track: audioTracks){
                    if(track.trackNumber == audioTracks.size()){
                        track.lowerSeparator.setVisible(false);
                    }
                    else{
                        track.lowerSeparator.setVisible(true);
                    }
                }
            }
        });

        // The PPR button will play all tracks from their current position.
        // When not synced, the PPR button should read 'Pause' only if all audio tracks are playing.
        // And it should read 'Play' otherwise.
        // If it reads 'Play' it should force all tracks to play when pressed.
        // If it reads 'Pause' it should force all tracks to pause when pressed.
        // TODO: Make tracks play from position of master track when unsynced.
        PPRButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                if(PPRButton.getText().equals("Pause")){
                    for(AudioTrack track: audioTracks){
                        if(track.trackHasFile() && track.isPlaying){
                            // Pause all playing tracks.
                            track.pprOnAction();
                            if(track.atEndOfMedia){
                                track.pprOnAction();
                            }
                        }
                    }
                    PPRButton.setText("Play");
                }
                else if(PPRButton.getText().equals("Play")){
                    for(AudioTrack track: audioTracks){
                        if(track.trackHasFile() && !track.isPlaying){
                            // Play all paused tracks.
                            track.pprOnAction();
                        }
                    }
                    PPRButton.setText("Pause");
                }
                else if(PPRButton.getText().equals("Restart")){
                    // TODO: Fix bug: tracks do not restart when PPRButton reads 'Restart'.
                }
            }
        });

        timeSlider.onDragDetectedProperty().set(timeSliderOnDragDetectedEH);
        timeSlider.onMouseReleasedProperty().set(timeSliderOnMouseReleasedEH);
    }

    MasterTrackCoordinates getTrackCoordinates(){
        return (MasterTrackCoordinates) trackCoordinates;
    }

    /**
     * Binds the valueProperty of two sliders to each other. Binding is unidirectional.
     * @param sliderOne The first slider to bind.
     * @param sliderTwo The second slider to bind.
     */
    public void bindSliderValueProperties(Slider sliderOne, Slider sliderTwo) {
        // Has to be bidirectional, otherwise the master time slider won't scroll automatically when played.
        sliderTwo.valueProperty().bindBidirectional(sliderOne.valueProperty());
    }

    /**
     * Binds the textProperty of labelOne to labelTwo. Binding is unidirectional.
     * @param labelOne The label whose textProperty will be bound to labelTwo.
     * @param labelTwo The label whose textProperty will not be bound.
     */
    public void bindLabelTextProperties(Label labelOne, Label labelTwo){
        labelOne.textProperty().bind(labelTwo.textProperty());
    }

    /**
     * Binds properties of this master track and all AudioTracks needed to synchronize them.
     */
    void sync(){
        for(AudioTrack track: audioTracks){
            if(track.trackHasFile()) syncTrack(track);
        }
    }

    void syncTrack(AudioTrack track){
        // Bind master currentTimeLabel to track if track is longest.
        if(track.trackNumber == longestAudioTrack.trackNumber){
            bindLabelTextProperties(currentTimeLabel, track.currentTimeLabel);
        }

        // Update pause time so newly synced tracks snap to the master track time slider before playing for the first time.
        track.pauseTime = timeSlider.getValue();

        // Bind slider values to master slider values.
        bindSliderValueProperties(timeSlider, track.timeSlider);
        bindSliderValueProperties(volumeSlider, track.volumeSlider);

        // Disable redundant buttons.
        track.PPRButton.setDisable(true);
        track.timeSlider.setDisable(true);
        track.volumeSlider.setDisable(true);
    }

    /**
     * Unbinds all bound properties of the master track.
     */
    public void unSync(){
        for(AudioTrack track: audioTracks){
            if(track.trackHasFile()) unSyncTrack(track);
        }
    }

    void unSyncTrack(AudioTrack track){
        if(track.trackNumber == longestAudioTrack.trackNumber){
            currentTimeLabel.textProperty().unbind();
        }
        timeSlider.valueProperty().unbindBidirectional(track.timeSlider.valueProperty());
        volumeSlider.valueProperty().unbindBidirectional(track.volumeSlider.valueProperty());
        track.timeSlider.valueProperty().unbindBidirectional(timeSlider.valueProperty());
        track.volumeSlider.valueProperty().unbindBidirectional(volumeSlider.valueProperty());

        track.PPRButton.setDisable(false);
        track.timeSlider.setDisable(false);
        track.volumeSlider.setDisable(false);
    }

    /**
     * Determines if any audio track is focused.
     * @return The focused track if one exists, 0 otherwise.
     */
    private String getFocusTrack(){
        int notMutedTracks = 0;
        int lastNotMutedTrack = 0;
        for(AudioTrack track: audioTracks){
            if(track.volumeSlider.getValue() == 0.0){
                notMutedTracks++;
                lastNotMutedTrack = track.trackNumber;
            }
        }
        if(notMutedTracks == 1){
            return String.valueOf(lastNotMutedTrack);
        }
        else{
            return "None";
        }
    }

    void removeAudioTrack(AudioTrack track){
        int removedTrackNumber = track.trackNumber;
        removeAudioTrackBackendAdjust(track);
        removeAudioTrackGUIAdjust(track, removedTrackNumber);
    }

    private void removeAudioTrackGUIAdjust(AudioTrack track, int removedTrackNumber){
        controller.removeAudioTrack(track);
        shiftTracksUp(removedTrackNumber);
        controller.resizeStageForAudioTrackChange();
        setSeparatorVisibilities();

        if(numberOfAudioTracks < MAX_TRACKS){
            addTrackButton.setDisable(false);
        }
        refreshDisabledStatus();
    }

    private void removeAudioTrackBackendAdjust(AudioTrack track){
        // Special case where the track to remove has no file.
        if(!track.trackHasFile()){
            removeFromAudioTracks(track);
            return;
        }

        // Special case where the last track is removed.
        if(audioTracks.size() == 1){
            if(synced){
                if(PPRButton.getText().equals("Pause")) PPRButton.fire();
                syncButton.fire();
            }
            removeFromAudioTracks(track);
            return;
        }

        // Determine if the track to be removed is focused.
        boolean trackFocused = false;
        if(track.focused){
            trackFocused = true;
        }

        // If needed, unsync the track to prepare it for removal.
        if(synced){
            unSyncTrack(track);
        }

        // Stop the tracks media player.
        track.mediaPlayer.stop();

        // Determine if the track to remove is the longest track.
        boolean longestTrackRemoved = false;
        if(track.trackNumber == longestAudioTrack.trackNumber){
            longestTrackRemoved = true;
        }

        removeFromAudioTracks(track);

        // Resync the longest track if needed.
        if(synced){
            if(trackFocused){
                // Resync volume sliders of remaining tracks.
                for(AudioTrack audioTrack: audioTracks){
                    bindSliderValueProperties(volumeSlider, audioTrack.volumeSlider);
                }
            }
            else if(longestTrackRemoved){
                if(longestAudioTrack != null){
                    syncTrack(longestAudioTrack);
                }
            }
        }
    }

    private void removeFromAudioTracks(AudioTrack track){
        // Remove the track and update the track numbers.
        audioTracks.remove(track);
        refreshTrackNumbers();

        // Remove the track from the sorted list and update the longest track.
        audioTracksSortedByDuration.remove(track);
        refreshLongestAudioTrack();

        numberOfAudioTracks--;
    }

    private void refreshTrackNumbers(){
        for(int i = 0; i < audioTracks.size(); i++){
            AudioTrack track = audioTracks.get(i);
            track.trackNumber = i + 1;
            track.trackLabel.setText("Track " + track.trackNumber);
        }
    }

    private void shiftTracksUp(int gap){
        for(AudioTrack audioTrack: audioTracks){
            if(audioTrack.trackNumber >= gap){
                audioTrack.shiftTrackUp();
            }
        }
    }

    private void setSeparatorVisibilities(){
        for(Track audioTrack: audioTracks){
            if(audioTrack.trackNumber == audioTracks.size()){
                audioTrack.lowerSeparator.setVisible(false);
            }
            else{
                audioTrack.lowerSeparator.setVisible(true);
            }
        }
    }

    void refreshLongestAudioTrack(){
        if(audioTracksSortedByDuration.size() > 0){
            bubbleSortAudioTracksByDuration();
            longestAudioTrack = audioTracksSortedByDuration.get(audioTracksSortedByDuration.size() - 1);

            // These properties don't need to be bound with formal bindings.
            totalTimeLabel.setText(longestAudioTrack.totalTimeLabel.getText());
            timeSlider.setMax(longestAudioTrack.timeSlider.getMax());
        }
        else{
            longestAudioTrack = null;
            totalTimeLabel.setText("00:00");
            timeSlider.setMax(TIME_SLIDER_DEFAULT_MAX);
        }
    }

    void bubbleSortAudioTracksByDuration(){
        int n = audioTracksSortedByDuration.size();
        int i, j;
        AudioTrack temp;
        boolean swapped;
        for (i = 0; i < n - 1; i++) {
            swapped = false;
            for (j = 0; j < n - i - 1; j++) {
                if(audioTracksSortedByDuration.get(j).mediaPlayer.getTotalDuration().toSeconds() > audioTracksSortedByDuration.get(j + 1).mediaPlayer.getTotalDuration().toSeconds()) {
                    // Swap arr[j] and arr[j+1]
                    temp = audioTracksSortedByDuration.get(j);
                    audioTracksSortedByDuration.set(j, audioTracksSortedByDuration.get(j + 1));
                    audioTracksSortedByDuration.set(j + 1, temp);
                    swapped = true;
                }
            }

            // If no two elements were
            // swapped by inner loop, then break
            if (!swapped)
                break;
        }
    }

    boolean someTrackHasFile(){
        for(AudioTrack track: audioTracks){
            if(track.trackHasFile()) return true;
        }
        return false;
    }

    void refreshDisabledStatus(){
        if(someTrackHasFile()){
            PPRButton.setDisable(false);
            syncButton.setDisable(false);
            timeSlider.setDisable(false);
        }
        else{
            timeSliderSetDefaultState();
            pprButtonSetDefaultState();
            syncButtonSetDefaultState();
            timeLabelSetDefaultState(currentTimeLabel);
        }
    }

    void timeSliderSetDefaultState(){
        timeSlider.setValue(0.0);
        timeSlider.setDisable(true);
    }

    void pprButtonSetDefaultState(){
        PPRButton.setText("Play");
        PPRButton.setDisable(true);
    }

    void syncButtonSetDefaultState(){
        syncButton.setText("Sync");
        synced = false;
        syncButton.setDisable(true);
    }

    void timeLabelSetDefaultState(Label timeLabel){
        timeLabel.setText("00:00 /");
    }

    void printAudioTracksSortedByDuration(){
        for(int i = 0; i < audioTracksSortedByDuration.size(); i++){
            AudioTrack track = audioTracksSortedByDuration.get(i);
            if(track.mediaPlayer == null){
                System.out.println("audioTracksSortedByDuration[" + i + "] = " + track.trackNumber + " (null)");
            }
            else{
                System.out.println("audioTracksSortedByDuration[" + i + "] = " + track.trackNumber + " (" + track.mediaPlayer.getTotalDuration().toSeconds() + ")");
            }
        }
    }
}
