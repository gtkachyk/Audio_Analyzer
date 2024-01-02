package gui.audioanalyzer;

import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import java.util.ArrayList;

public class MasterTrack extends Track{

    // Constants.
    private static final double ADD_TRACK_BUTTON_WIDTH = 69.6;
    private static final double ADD_TRACK_BUTTON_HEIGHT = 25.6;
    static final double MILLISECONDS_PER_SECOND = 1000.0;
    static final int MAX_TRACKS = 10;
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
    @FXML
    Button debugReportButton;

    // Other data.
    boolean synced = false;
    ArrayList<AudioTrack> audioTracks = new ArrayList<>(); // Track #n = audioTracks.get(n - 1).
    ArrayList<AudioTrack> audioTracksSortedByDuration = new ArrayList<>(); // Sorted by increasing order of MediaPlayer duration.
    AudioTrack longestAudioTrack = null;
    int numberOfAudioTracks = 0;

    ChangeListener<Number> timeSliderChangeListener;
    EventHandler<MouseEvent> switchButtonOnMouseClickedEH;
    EventHandler<MouseEvent> timeSliderOnDragDetectedEH;
    EventHandler<MouseEvent> timeSliderOnMouseReleasedEH;
    EventHandler<ActionEvent> syncButtonOnActionEH;
    EventHandler<ActionEvent> addTrackButtonOnAction;
    EventHandler<ActionEvent> pprButtonOnActionEH;
    EventHandler<ActionEvent> debugButtonOnActionEH;

    public MasterTrack(MasterTrackCoordinates masterTrackCoordinates, MainController controller){
        Track.controller = controller;
        trackNumber = 0;
        trackCoordinates = masterTrackCoordinates;

        instantiateJavaFXObjects();
        initializeJavaFXObjects();
        setJavaFXObjectsDefaultProperties();
        initializeTrack();
    }

    void instantiateJavaFXObjects(){
        trackLabel = new Label();
        focusTrackLabel = new Label();
        lowerVolumeLabel = new Label();
        volumeSlider = new Slider();
        raiseVolumeLabel = new Label();
        PPRButton = new Button();
        timeSlider = new Slider();
        currentTimeLabel = new Label();
        totalTimeLabel = new Label();
        switchButton = new Button();
        syncButton = new Button();
        addTrackButton = new Button();
        lowerSeparator = new Separator();
        debugReportButton = new Button();
    }

    void initializeJavaFXObjects(){
        initializeTrackObject(trackLabel, getTrackCoordinates().trackLabelX, getTrackCoordinates().trackLabelY, TRACK_LABEL_WIDTH, LABEL_HEIGHT);
        initializeTrackObject(focusTrackLabel, getTrackCoordinates().focusTrackLabelX, getTrackCoordinates().focusTrackLabelY, AUDIO_LABEL_WIDTH, LABEL_HEIGHT);
        initializeTrackObject(lowerVolumeLabel, getTrackCoordinates().lowerVolumeLabelX, getTrackCoordinates().lowerVolumeLabelY, LOWER_VOLUME_LABEL_WIDTH, LABEL_HEIGHT);
        initializeTrackObject(volumeSlider, getTrackCoordinates().volumeSliderX, getTrackCoordinates().volumeSliderY, VOLUME_SLIDER_WIDTH, SLIDER_HEIGHT);
        initializeTrackObject(raiseVolumeLabel, getTrackCoordinates().raiseVolumeLabelX, getTrackCoordinates().raiseVolumeLabelY, RAISE_VOLUME_LABEL_WIDTH, LABEL_HEIGHT);
        initializeTrackObject(PPRButton, getTrackCoordinates().PPRButtonX, getTrackCoordinates().PPRButtonY, PPR_BUTTON_WIDTH, PPR_BUTTON_HEIGHT);
        initializeTrackObject(timeSlider, getTrackCoordinates().timeSliderX, getTrackCoordinates().timeSliderY, TIME_SLIDER_WIDTH, SLIDER_HEIGHT);
        initializeTrackObject(currentTimeLabel, getTrackCoordinates().currentTimeLabelX, getTrackCoordinates().currentTimeLabelY, CURRENT_TIME_LABEL_WIDTH, LABEL_HEIGHT);
        initializeTrackObject(totalTimeLabel, getTrackCoordinates().totalTimeLabelX, getTrackCoordinates().totalTimeLabelY, TOTAL_TIME_LABEL_WIDTH, LABEL_HEIGHT);
        initializeTrackObject(switchButton, getTrackCoordinates().switchButtonX, getTrackCoordinates().switchButtonY, PPR_BUTTON_WIDTH, PPR_BUTTON_HEIGHT);
        initializeTrackObject(syncButton, getTrackCoordinates().syncButtonX, getTrackCoordinates().syncButtonY, PPR_BUTTON_WIDTH, PPR_BUTTON_HEIGHT);
        initializeTrackObject(addTrackButton, getTrackCoordinates().addTrackButtonX, getTrackCoordinates().addTrackButtonY, ADD_TRACK_BUTTON_WIDTH, ADD_TRACK_BUTTON_HEIGHT);
        initializeTrackObject(lowerSeparator, 0.0, MasterTrackCoordinates.MASTER_TRACK_SEPARATOR_Y_COORDINATE, SEPARATOR_WIDTH, SEPARATOR_HEIGHT);
        initializeTrackObject(debugReportButton, getTrackCoordinates().addTrackButtonX - 100.0, getTrackCoordinates().addTrackButtonY, ADD_TRACK_BUTTON_WIDTH, ADD_TRACK_BUTTON_HEIGHT);
    }

    void setJavaFXObjectsDefaultProperties(){
        trackLabel.setText("Master");
        focusTrackLabel.setText("Focus Track: None");
        lowerVolumeLabel.setText("-");
        volumeSlider.setMax(VOLUME_SLIDER_MAX);
        volumeSlider.setValue(volumeSlider.getMax());
        raiseVolumeLabel.setText("+");
        PPRButton.setDisable(true);
        PPRButton.setText("Play");
        timeSlider.setDisable(true);
        currentTimeLabel.setText("00:00 / ");
        totalTimeLabel.setText("00:00");
        switchButton.setText("Switch");
        switchButton.setDisable(true);
        syncButton.setText("Sync");
        syncButton.setDisable(true);
        addTrackButton.setText("Add Track");
        debugReportButton.setText("Debug");
        debugReportButton.setVisible(false); // Set to true to debug.
    }

    @Override
    void initializeTrack() {
        MasterTrackListeners.addSyncButtonOnActionEH(MasterTrack.this);
        MasterTrackListeners.addAddTrackButtonOnAction(MasterTrack.this);
        MasterTrackListeners.addPPRButtonOnActionEH(MasterTrack.this);
        MasterTrackListeners.addTimeSliderOnDragDetectedEH(MasterTrack.this);
        MasterTrackListeners.addTimeSliderOnMouseReleasedEH(MasterTrack.this);
        MasterTrackListeners.addSwitchButtonOnMouseClickedEH(MasterTrack.this);
        MasterTrackListeners.addDebugButtonOnMouseActionEH(MasterTrack.this);
    }

    MasterTrackCoordinates getTrackCoordinates(){
        return (MasterTrackCoordinates) trackCoordinates;
    }

    /**
     * Binds properties of this master track and all AudioTracks needed to synchronize them.
     */
    void sync(){
        for(AudioTrack track: audioTracks){
            if(track.trackHasFile()){
                syncTrack(track);
            }
        }
    }

    void syncTrack(AudioTrack track){
        // Bind master currentTimeLabel to track if track is longest.
        // System.out.println("In syncTrack(): track.trackNumber = " + track.trackNumber + ", longestAudioTrack.trackNumber = " + longestAudioTrack.trackNumber);
        if(track.trackNumber == longestAudioTrack.trackNumber){
            MasterTrackListeners.bindLabelTextProperties(currentTimeLabel, track.currentTimeLabel);
        }

        // Update pause time so newly synced tracks snap to the master track time slider before playing for the first time.
        track.pauseTime = timeSlider.getValue();

        // Bind slider values to master slider values.
        MasterTrackListeners.bindSliderValueProperties(timeSlider, track.timeSlider);
        MasterTrackListeners.bindSliderValueProperties(volumeSlider, track.volumeSlider);

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
            if(track.trackHasFile()){
                unSyncTrack(track);
            }
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

    void refreshSync(){
        unSync();
        sync();
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
            if(PPRButton.getText().equals("Pause")) PPRButton.fire();
            if(synced){
                syncButton.fire();
            }
            removeFromAudioTracks(track);
            return;
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
            if(longestTrackRemoved){
                if(longestAudioTrack != null){
                    syncTrack(longestAudioTrack);
                }
            }
        }
        if(isSomeTrackFocused()){
            refreshFocus();
        }
        else{
            refreshUnFocus();
        }
        setSwitchDisabled();
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

    /**
     * Determines if at least one track in audioTracks has a valid file associated with it.
     * @return True if some track has a valid file, false otherwise.
     */
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

    boolean isSomeTrackFocused(){
        for(AudioTrack track: audioTracks){
            if(track.focused) return true;
        }
        return false;
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

    void refreshFocus(){
        for(AudioTrack track: audioTracks){
            if(track.focused) {
                track.focusTrack();
            }
        }
    }

    void refreshUnFocus(){
        for(AudioTrack track: audioTracks){
            if(!track.focused) {
                track.undoFocus();
            }
        }
    }

    AudioTrack getFocusedTrack(){
        for(AudioTrack track: audioTracks){
            if(track.focused) return track;
        }
        return null;
    }

    boolean onlyOneTrackHasFile(){
        int tracksWithFiles = 0;
        for(AudioTrack track: audioTracks){
            if(track.trackHasFile()){
                tracksWithFiles++;
            }
        }
        return tracksWithFiles < 2;
    }

    void setSwitchDisabled(){
        if(!isSomeTrackFocused()){
            switchButton.setDisable(true);
        }
        else if(!someTrackHasFile()){
            switchButton.setDisable(true);
        }
        else if(onlyOneTrackHasFile()){
            switchButton.setDisable(true);
        }
        else{
            switchButton.setDisable(false);
        }
    }
}
