package gui.audioanalyzer;

import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;

public class AudioTrack extends Track{

    // Constants.
    static final double REMOVE_TRACK_BUTTON_SIZE = 10.0; // The button is a square. Original value 15.0

    // JavaFX objects.
    @FXML
    Label audioLabel;
    @FXML
    Button removeTrackButton;

    // Listeners (14).
    EventHandler<ActionEvent> pprButtonOnActionEH;
    ChangeListener<String> pprButtonTextPropertyCL;
    InvalidationListener volumeSliderValuePropertyIL;
    ChangeListener<Boolean> timeSliderValueChangingCL;
    ChangeListener<Number> timeSliderValueCL;
    ChangeListener<Duration> mediaPlayerCurrentTimeCL;
    Runnable mediaPlayerOnEndOfMediaR;
    EventHandler<MouseEvent> timeSliderOnMouseClickedEH;
    EventHandler<MouseEvent> timeSliderOnMouseReleasedEH;
    EventHandler<MouseEvent> timeSliderOnDragDetectedEH;
    EventHandler<MouseEvent> audioLabelOnMouseClickedEH;
    EventHandler<MouseEvent> trackLabelOnMouseClickedEH;
    EventHandler<MouseEvent> removeTrackButtonOnClickEH;
    Runnable mediaPlayerOnReadyR;

    // State tracking fields.
    boolean isPlaying = false;
    boolean isMuted = false;
    boolean atEndOfMedia = false;
    boolean focused = false;
    boolean synced = false;
    double pauseTime;

    // Audio fields.
    File audioFile;
    Media media;
    MediaPlayer mediaPlayer;

    // The master track that controls this audio track.
    MasterTrack masterTrack;

    public AudioTrack(int trackNumber, AudioTrackCoordinates coordinates, MasterTrack masterTrack){
        this.masterTrack = masterTrack;
        this.trackNumber = trackNumber;
        trackCoordinates = coordinates;

        initializeTrack();
    }

    // ------------------------------------------------------------------------------------------------------------
    // --------------------------------------------- Setup Methods ------------------------------------------------
    // ------------------------------------------------------------------------------------------------------------

    void instantiateJavaFXObjects(){
        trackLabel = new Label();
        audioLabel = new Label();
        lowerVolumeLabel = new Label();
        volumeSlider = new Slider();
        raiseVolumeLabel = new Label();
        PPRButton = new Button();
        timeSlider = new Slider();
        currentTimeLabel = new Label();
        totalTimeLabel = new Label();
        removeTrackButton = new Button();
        lowerSeparator = new Separator();
    }

    void initializeJavaFXObjects(){
        initializeTrackObject(trackLabel, getTrackCoordinates().trackLabelX, getTrackCoordinates().trackLabelY, TRACK_LABEL_WIDTH, LABEL_HEIGHT);
        initializeTrackObject(audioLabel, getTrackCoordinates().audioLabelX, getTrackCoordinates().audioLabelY, AUDIO_LABEL_WIDTH, LABEL_HEIGHT);
        initializeTrackObject(lowerVolumeLabel, getTrackCoordinates().lowerVolumeLabelX, getTrackCoordinates().lowerVolumeLabelY, LOWER_VOLUME_LABEL_WIDTH, LABEL_HEIGHT);
        initializeTrackObject(volumeSlider, getTrackCoordinates().volumeSliderX, getTrackCoordinates().volumeSliderY, VOLUME_SLIDER_WIDTH, SLIDER_HEIGHT);
        initializeTrackObject(raiseVolumeLabel, getTrackCoordinates().raiseVolumeLabelX, getTrackCoordinates().raiseVolumeLabelY, RAISE_VOLUME_LABEL_WIDTH, LABEL_HEIGHT);
        initializeTrackObject(PPRButton, getTrackCoordinates().PPRButtonX, getTrackCoordinates().PPRButtonY, PPR_BUTTON_WIDTH, PPR_BUTTON_HEIGHT);
        initializeTrackObject(timeSlider, getTrackCoordinates().timeSliderX, getTrackCoordinates().timeSliderY, TIME_SLIDER_WIDTH, SLIDER_HEIGHT);
        initializeTrackObject(currentTimeLabel, getTrackCoordinates().currentTimeLabelX, getTrackCoordinates().currentTimeLabelY, CURRENT_TIME_LABEL_WIDTH, LABEL_HEIGHT);
        initializeTrackObject(totalTimeLabel, getTrackCoordinates().totalTimeLabelX, getTrackCoordinates().totalTimeLabelY, TOTAL_TIME_LABEL_WIDTH, LABEL_HEIGHT);
        initializeTrackObject(removeTrackButton, getTrackCoordinates().removeTrackButtonX, getTrackCoordinates().removeTrackButtonY, REMOVE_TRACK_BUTTON_SIZE, REMOVE_TRACK_BUTTON_SIZE);
        initializeTrackObject(lowerSeparator, getTrackCoordinates().upperSeparatorX, getTrackCoordinates().upperSeparatorY + AudioTrackCoordinates.AUDIO_TRACK_HEIGHT, SEPARATOR_WIDTH, SEPARATOR_HEIGHT);
    }

    void setJavaFXObjectsDefaultProperties(){
        trackLabel.setText("Track " + trackNumber);
        lowerVolumeLabel.setText("-");
        volumeSlider.setMax(VOLUME_SLIDER_MAX);
        volumeSlider.setValue(volumeSlider.getMax());
        volumeSlider.setDisable(true);
        raiseVolumeLabel.setText("+");
        PPRButton.setDisable(true);
        PPRButton.setText("Play");
        timeSlider.setDisable(true);
        removeTrackButton.setText("X");

//        Image imageRemove = new Image(new File("src/images/remove_track_button_image.png").toURI().toString());
//        ImageView ivRemove = new ImageView(imageRemove);
//        ivRemove.setFitHeight(5.0);
//        ivRemove.setFitWidth(5.0);
//        removeTrackButton.setGraphic(ivRemove);
//        removeTrackButton.setStyle("-fx-background-color: #e81123;");

        audioLabel.setText("Add file...");
    }

    @Override
    void initializeTrack(){
        instantiateJavaFXObjects();
        initializeJavaFXObjects();
        setJavaFXObjectsDefaultProperties();
        setTheme();
        AudioTrackListeners.addStableListeners(AudioTrack.this);
    }

    // ------------------------------------------------------------------------------------------------------------
    // --------------------------------------------- Update File Methods ------------------------------------------
    // ------------------------------------------------------------------------------------------------------------

    private boolean isValidFile(File file){
        try{
            Media testMedia = new Media(file.toURI().toString());
            testMedia = null;
            return true;
        }
        catch(MediaException e){
            controller.createAlert(new Alert(Alert.AlertType.ERROR, "Unsupported file type selected.", ButtonType.OK), "Error").showAndWait();
            return false;
        }
    }

    void getNewAudioFile(){
        // Open the file browser at the current directory.
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File("."));
        fileChooser.setTitle("Select a file for track " + trackNumber + "...");
        updateFile(fileChooser.showOpenDialog(new Stage()));
    }

    private void setTrackAudio(File audioFile){
        media = new Media(audioFile.toURI().toString());
        mediaPlayer = new MediaPlayer(media);
        this.audioFile = audioFile;
    }

    private void addNewAudioFile(File newFile){
        // Add media.
        setTrackAudio(newFile);

        // Bidirectionally bind volume slider value to volume property of media player.
        mediaPlayer.volumeProperty().bindBidirectional(volumeSlider.valueProperty());

        // Add listeners.
        AudioTrackListeners.addUnstableListeners(AudioTrack.this);

        // Only add tracks with valid files to sorted list.
        masterTrack.audioTracksSortedByDuration.add(AudioTrack.this);
    }

    private void changeAudioFile(File newFile){
        // Stop media.
        mediaPlayer.stop();

        // Unbind properties.
        mediaPlayer.volumeProperty().unbindBidirectional(volumeSlider.valueProperty());
        volumeSlider.valueProperty().unbindBidirectional(mediaPlayer.volumeProperty());
        currentTimeLabel.textProperty().unbind();

        // Update media.
        setTrackAudio(newFile);
    }

    void updateFile(File newFile){
        if(newFile == null || !isValidFile(newFile)) return;

        TrackUtilities.resetAllTracks(masterTrack);
        if(trackHasFile()){
            changeAudioFile(newFile);
        }
        else {
            addNewAudioFile(newFile);
        }
        AudioTrackListeners.refreshListeners(AudioTrack.this);
        TrackUtilities.resetAllTracks(masterTrack);
    }

    // ------------------------------------------------------------------------------------------------------------
    // --------------------------------------------- Focusing Methods ---------------------------------------------
    // ------------------------------------------------------------------------------------------------------------

    /**
     * Focuses this audio track.
     */
    void focusTrack(){
        for(AudioTrack track: masterTrack.audioTracks){
            if(masterTrack.synced && track.trackNumber != trackNumber){
                Bindings.unbindBidirectional(masterTrack.volumeSlider.valueProperty(), track.volumeSlider.valueProperty());
                Bindings.unbindBidirectional(track.volumeSlider.valueProperty(), masterTrack.volumeSlider.valueProperty());
            }
            setTrackOutOfFocus(track);
        }
        setTrackInFocus(AudioTrack.this);
        masterTrack.focusTrackLabel.setText("Focus Track: " + trackNumber);
        if(masterTrack.synced){
            MasterTrackListeners.bindSliderValueProperties(masterTrack.volumeSlider, volumeSlider);
        }
        masterTrack.refreshSwitchDisabledStatus();
    }

    void undoFocus(){
        focused = false;
        trackLabel.borderProperty().set(null);
        for(AudioTrack track: masterTrack.audioTracks){
            if(track.trackNumber != trackNumber){
                track.volumeSlider.setValue(1.0);
                if(masterTrack.synced && track.trackHasFile()){
                    MasterTrackListeners.bindSliderValueProperties(track.volumeSlider, masterTrack.volumeSlider);
                }
            }
        }
        masterTrack.refreshSwitchDisabledStatus();
    }

    private void setTrackInFocus(AudioTrack track){
        track.volumeSlider.setValue(1.0);
        track.focused = true;
        if(controller.darkMode){
            track.trackLabel.setBorder(new Border(new BorderStroke(Color.WHITE, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
        }
        else{
            track.trackLabel.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
        }
    }

    private void setTrackOutOfFocus(AudioTrack track){
        track.volumeSlider.setValue(0.0);
        track.focused = false;
        track.trackLabel.borderProperty().set(null);
    }

    // ------------------------------------------------------------------------------------------------------------
    // --------------------------------------------- GUI Methods --------------------------------------------------
    // ------------------------------------------------------------------------------------------------------------

    /**
     * Moves the coordinates of all GUI objects of this track up by one track size.
     */
    void shiftTrackUp(){
        shiftTrackObjectUp(lowerSeparator, lowerSeparator.getLayoutY());
        shiftTrackObjectUp(trackLabel, trackLabel.getLayoutY());
        shiftTrackObjectUp(audioLabel, audioLabel.getLayoutY());
        shiftTrackObjectUp(lowerVolumeLabel, lowerVolumeLabel.getLayoutY());
        shiftTrackObjectUp(volumeSlider, volumeSlider.getLayoutY());
        shiftTrackObjectUp(raiseVolumeLabel, raiseVolumeLabel.getLayoutY());
        shiftTrackObjectUp(PPRButton, PPRButton.getLayoutY());
        shiftTrackObjectUp(timeSlider, timeSlider.getLayoutY());
        shiftTrackObjectUp(currentTimeLabel, currentTimeLabel.getLayoutY());
        shiftTrackObjectUp(totalTimeLabel, totalTimeLabel.getLayoutY());
        shiftTrackObjectUp(removeTrackButton, removeTrackButton.getLayoutY());
    }

    void shiftTrackObjectUp(Object trackObject, double objectLayoutY){
        Class<?> classObject = trackObject.getClass();
        try{
            Method setLayoutY = classObject.getMethod("setLayoutY", double.class);
            setLayoutY.invoke(trackObject, objectLayoutY - AudioTrackCoordinates.AUDIO_TRACK_HEIGHT);
        }
        catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e){
            e.printStackTrace();
        }
    }

    private void setTheme(){
        if(controller.darkMode){
            trackLabel.textFillProperty().set(Color.WHITE);
            audioLabel.textFillProperty().set(Color.WHITE);
            lowerVolumeLabel.textFillProperty().set(Color.WHITE);
            raiseVolumeLabel.textFillProperty().set(Color.WHITE);
            currentTimeLabel.textFillProperty().set(Color.WHITE);
            totalTimeLabel.textFillProperty().set(Color.WHITE);
        }
    }

    // ------------------------------------------------------------------------------------------------------------
    // --------------------------------------------- Utility Methods ----------------------------------------------
    // ------------------------------------------------------------------------------------------------------------

    public void bindCurrentTimeLabel(){
        currentTimeLabel.textProperty().bind(Bindings.createStringBinding(new Callable<String>() {
            @Override
            public String call() {
                // Check where the time slider is.
                if(timeSlider.getValue() == timeSlider.getMax()){
                    if(masterTrack.synced){
                        return getTime(masterTrack.shortestAudioTrack.mediaPlayer.getTotalDuration()) + " / ";
                    }
                    else{
                        return getTime(mediaPlayer.getTotalDuration()) + " / ";
                    }
                }
                else if(timeSlider.getValue() == 0.0){
                    return getTime(mediaPlayer.getStartTime()) + " / ";
                }
                else{
                    return getTime(mediaPlayer.getCurrentTime()) + " / ";
                }
            }
        }, mediaPlayer.currentTimeProperty()));
    }

    void labelMatchEndSong(String labelTime, String labelTotalTime){
        boolean updateMasterPPRText = false;
        if(masterTrack.synced && masterTrack.shortestAudioTrack != null && TrackUtilities.trackEquals(AudioTrack.this, masterTrack.shortestAudioTrack)){
            updateMasterPPRText = true;
        }

        if(!updateMasterPPRText){
            if(masterTrack.synced) return;
            for(int i = 0; i < labelTotalTime.length(); i++){
                if(labelTime.charAt(i) != labelTotalTime.charAt(i)){
                    atEndOfMedia = false;
                    if(isPlaying){
                        PPRButton.setText("Pause");
                    }
                    else{
                        PPRButton.setText("Play");
                    }
                    return;
                }
            }
            atEndOfMedia = true;
            PPRButton.setText("Restart");
            return;
        }
        else{
            for(int i = 0; i < labelTotalTime.length(); i++){
                if(labelTime.charAt(i) != labelTotalTime.charAt(i)){
                    atEndOfMedia = false;
                    if(isPlaying){
                        PPRButton.setText("Pause");
                        // masterTrack.PPRButton.setText("Pause");
                    }
                    else{
                        PPRButton.setText("Play");
                        // masterTrack.PPRButton.setText("Play");
                    }
                    return;
                }
            }
            atEndOfMedia = true;
            PPRButton.setText("Restart");
            // masterTrack.PPRButton.setText("Restart");
            return;
        }
    }

    AudioTrackCoordinates getTrackCoordinates(){
        return (AudioTrackCoordinates) trackCoordinates;
    }

    boolean trackHasFile(){
        return (audioFile != null) && (media != null) && (mediaPlayer != null);
    }

    void playTrack(){
        mediaPlayer.seek(Duration.seconds(pauseTime));
        mediaPlayer.play();
        PPRButton.setText("Pause");
        isPlaying = true;
    }

    void pauseTrack(){
        mediaPlayer.pause();
        pauseTime = mediaPlayer.getCurrentTime().toSeconds();
        PPRButton.setText("Play");
        isPlaying = false;
    }

    void restartTrack(){
        pauseTime = 0.0;
        timeSlider.setValue(0.0);
        playTrack();
        atEndOfMedia = false;
    }

    void printState(){
        System.out.println("Track number: " + trackNumber);
        if(trackHasFile()){
            System.out.println("audioFile.getName(): " + audioFile.getName());
            System.out.println("media.toString(): " + media.toString());
            System.out.println("mediaPlayer.getStatus(): " + mediaPlayer.getStatus());
            System.out.println("atEndOfMedia: " + atEndOfMedia);
            System.out.println("isPlaying: " + isPlaying);
            System.out.println("isMuted: " + isMuted);
            System.out.println("pauseTime: " + pauseTime);
            System.out.println("focused: " + focused);
            System.out.println("synced: " + synced);

            System.out.println("volumeSlider.getValue(): " + volumeSlider.getValue());
            System.out.println("timeSlider.getValue(): " + timeSlider.getValue());
            System.out.println("mediaPlayer.getCurrentTime(): " + mediaPlayer.getCurrentTime().toSeconds());
        }
        else{
            System.out.println("<no file>");
        }
    }
}
