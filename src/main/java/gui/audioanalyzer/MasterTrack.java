package gui.audioanalyzer;

import gui.audioanalyzer.exceptions.TrackRemoveException;
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

public class MasterTrack extends Track {

    // Constants.
    private static final double ADD_TRACK_BUTTON_WIDTH = 69.6;
    private static final double ADD_TRACK_BUTTON_HEIGHT = 25.6;
    static final double MILLISECONDS_PER_SECOND = 1000.0;
    static final int MAX_TRACKS = 10;
    static final double TIME_SLIDER_DEFAULT_MAX = 100.0;

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
        raiseVolumeLabel.setText("+");
        totalTimeLabel.setText("00:00");
        switchButton.setText("Switch");
        addTrackButton.setText("Add Track");
        debugReportButton.setText("Debug");
        setGUIDefaultState();
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
        MasterTrackListeners.addTimeSliderChangeListener(MasterTrack.this);

        MasterTrackListeners.addDebugButtonOnMouseActionEH(MasterTrack.this);
    }


    // ------------------------------------------------------------------------------------------------------------
    // --------------------------------------------- Syncing Methods ----------------------------------------------
    // ------------------------------------------------------------------------------------------------------------

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

    private void syncTrack(AudioTrack track){
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

        track.synced = true;
    }

    /**
     * Unbinds all bound properties of the master track.
     */
    void unSync(){
        for(AudioTrack track: audioTracks){
            if(track.trackHasFile()){
                unSyncTrack(track);
            }
        }
    }

    private void unSyncTrack(AudioTrack track){
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

        track.synced = false;
    }

    void refreshSync(){
        unSync();
        sync();
    }

    // ------------------------------------------------------------------------------------------------------------
    // --------------------------------------------- Track Removal Methods ----------------------------------------
    // ------------------------------------------------------------------------------------------------------------

    void removeAudioTrack(AudioTrack track){
        int removedTrackNumber = track.trackNumber;
        try{
            removeAudioTrackBackendAdjust(track);
        }
        catch (TrackRemoveException e){
            e.printStackTrace();
        }
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

    private void removeAudioTrackBackendAdjust(AudioTrack track) throws TrackRemoveException {
        prepareTrackForRemoval(track);

        // Special case: The track to remove has no file.
        if(!track.trackHasFile()){
            removeEmptyTrack(track);
            return;
        }

        // Special case: The last track is removed.
        if(audioTracks.size() == 1){
            removeLastTrack(track);
            return;
        }

        if(TrackUtilities.trackEquals(track, longestAudioTrack)){
            removeLongestTrack(track);
        }
        else{
            removeFromAudioTracks(track);
        }
        refreshFocusState();
    }

    /**
     * Prepares an AudioTrack to be removed from audioTracks.
     * @param track The track to prepare.
     */
    private void prepareTrackForRemoval(AudioTrack track){
        if(track.synced) unSyncTrack(track);
        if(track.focused) track.undoFocus();
        if(track.mediaPlayer != null) track.mediaPlayer.stop();
    }

    /**
     * Removes an empty track from the back end.
     * @param track The empty track to remove.
     */
    private void removeEmptyTrack(AudioTrack track) throws TrackRemoveException {
        if(!TrackUtilities.canRemoveTrack(track)) throw new TrackRemoveException("Cannot remove empty track");
        removeFromAudioTracks(track);
    }

    /**
     * Removes the only track from the backend.
     * @param track The last track to remove.
     */
    private void removeLastTrack(AudioTrack track) throws TrackRemoveException {
        if(!TrackUtilities.canRemoveTrack(track)) throw new TrackRemoveException("Cannot remove empty track");
        if(PPRButton.getText().equals("Pause")) PPRButton.fire();
        if(synced){
            syncButton.fire();
        }
        removeFromAudioTracks(track);
    }

    private void removeLongestTrack(AudioTrack track) throws TrackRemoveException {
        if(!TrackUtilities.canRemoveTrack(track)) throw new TrackRemoveException("Cannot remove longest track");
        removeFromAudioTracks(track);

        // Resync the longest track if needed.
        // This is needed to rebind (unidirectional) the master track currentTimeLabel to the new longest track.
        if(synced && longestAudioTrack != null){
            MasterTrackListeners.bindLabelTextProperties(currentTimeLabel, longestAudioTrack.currentTimeLabel);
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

    // ------------------------------------------------------------------------------------------------------------
    // --------------------------------------------- State Managing Methods ---------------------------------------
    // ------------------------------------------------------------------------------------------------------------

    private void refreshTrackNumbers(){
        for(int i = 0; i < audioTracks.size(); i++){
            AudioTrack track = audioTracks.get(i);
            track.trackNumber = i + 1;
            track.trackLabel.setText("Track " + track.trackNumber);
        }
    }

    void refreshLongestAudioTrack(){
        if(audioTracksSortedByDuration.size() > 0){
            TrackUtilities.sortAudioTracksByDuration(audioTracksSortedByDuration);
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

    void refreshDisabledStatus(){
        if(TrackUtilities.someTrackHasFile(audioTracks)){
            setGUIActiveState();
        }
        else{
            setGUIDefaultState();
            synced = false;
        }
        setSwitchDisabled();
    }

    void setGUIActiveState(){
        PPRButton.setDisable(false);
        syncButton.setDisable(false);
        timeSlider.setDisable(false);
        volumeSlider.setDisable(false);
    }

    void setGUIDefaultState(){
        timeSlider.setValue(0.0);
        timeSlider.setDisable(true);
        PPRButton.setText("Play");
        PPRButton.setDisable(true);
        syncButton.setText("Sync");
        volumeSlider.setValue(1.0);
        volumeSlider.setDisable(true);
        currentTimeLabel.setText("00:00 /");
    }

    void refreshFocus(){
        for(AudioTrack track: audioTracks){
            if(track.focused) {
                track.focusTrack();
            }
        }
    }

    private void refreshUnFocus(){
        for(AudioTrack track: audioTracks){
            if(!track.focused) {
                track.undoFocus();
            }
        }
    }

    void setSwitchDisabled(){
        if(!TrackUtilities.isSomeTrackFocused(audioTracks)){
            switchButton.setDisable(true);
        }
        else if(!TrackUtilities.someTrackHasFile(audioTracks)){
            switchButton.setDisable(true);
        }
        else if(TrackUtilities.onlyOneTrackHasFile(audioTracks)){
            switchButton.setDisable(true);
        }
        else{
            switchButton.setDisable(false);
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

   private void refreshFocusState(){
        if(TrackUtilities.isSomeTrackFocused(audioTracks)){
            refreshFocus();
        }
        else{
            refreshUnFocus();
        }
        setSwitchDisabled();
    }

    void refreshPPRText(){
        if(!synced){
            int finishedTracks = 0;
            int playingTracks = 0;
            int pausedTracks = 0;
            for(AudioTrack track: audioTracks){
                if(track.trackHasFile()){
                    if(track.PPRButton.getText().equals("Play")){
                        pausedTracks++;
                    }
                    else if(track.PPRButton.getText().equals("Restart")){
                        finishedTracks++;
                    }
                    else{
                        playingTracks++;
                    }
                }
            }
            int nonEmptyTracks = audioTracks.size() - TrackUtilities.emptyTracks(audioTracks);
            if(finishedTracks == nonEmptyTracks){
                PPRButton.setText("Restart");
            }
            else if(playingTracks == nonEmptyTracks){
                PPRButton.setText("Pause");
            }
            else if(pausedTracks == nonEmptyTracks){
                PPRButton.setText("Play");
            }
            else{
                PPRButton.setText("Press All");
            }
        }
    }

    // ------------------------------------------------------------------------------------------------------------
    // --------------------------------------------- Utility Methods ----------------------------------------------
    // ------------------------------------------------------------------------------------------------------------

    private void shiftTracksUp(int gap){
        for(AudioTrack audioTrack: audioTracks){
            if(audioTrack.trackNumber >= gap){
                audioTrack.shiftTrackUp();
            }
        }
    }

    MasterTrackCoordinates getTrackCoordinates(){
        return (MasterTrackCoordinates) trackCoordinates;
    }

}
