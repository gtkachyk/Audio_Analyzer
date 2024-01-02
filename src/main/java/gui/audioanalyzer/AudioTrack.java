package gui.audioanalyzer;

import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;

public class AudioTrack extends Track{

    // Constants.
    static final double REMOVE_TRACK_BUTTON_SIZE = 15.0; // The button is a square. Original value 26.6.

    File audioFile;
    Media media;
    MediaPlayer mediaPlayer;

    boolean atEndOfMedia = false;
    boolean isPlaying = false;
    boolean isMuted = false;
    double pauseTime;
    boolean focused = false;
    boolean synced = false;
    MasterTrack masterTrack; // The master track that controls this audio track.

    // JavaFX objects.
    @FXML
    Label audioLabel;
    @FXML
    Button removeTrackButton;

    // Listeners (14).
    EventHandler<ActionEvent> pprButtonOnActionEH;
    ChangeListener<String> pprButtonTextPropertyCL;
    InvalidationListener volumeSliderValuePropertyIL;
    ChangeListener<Duration> mediaPlayerTotalDurationCL;
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

    public AudioTrack(int trackNumber, AudioTrackCoordinates coordinates, MasterTrack masterTrack){
        this.masterTrack = masterTrack;
        this.trackNumber = trackNumber;
        trackCoordinates = coordinates;

        initializeTrack();
    }

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
        removeTrackButton.setText("x");
        removeTrackButton.setMinWidth(REMOVE_TRACK_BUTTON_SIZE);
        removeTrackButton.setMinHeight(REMOVE_TRACK_BUTTON_SIZE);
        removeTrackButton.setFont(new Font(removeTrackButton.getFont().getName(), removeTrackButton.getFont().getSize() - 5.0));
        removeTrackButton.textAlignmentProperty().set(TextAlignment.CENTER);
        removeTrackButton.setBackground(new Background(new BackgroundFill(Color.RED, CornerRadii.EMPTY, Insets.EMPTY)));
        removeTrackButton.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
        audioLabel.setText("Add file...");
    }

    void setStateAfterFileChange(){
        atEndOfMedia = false;
        isPlaying = false;
        isMuted = false;
        pauseTime = 0.0;
        PPRButton.setText("Play");
        timeSlider.setValue(0.0);
    }

    @Override
    void initializeTrack(){
        instantiateJavaFXObjects();
        initializeJavaFXObjects();
        setJavaFXObjectsDefaultProperties();
        setTheme();
        AudioTrackListeners.addStableListeners(AudioTrack.this);
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

    private void setAudioLabelText(){
        if(audioFile == null){
            audioLabel.setText("Add file...");
        }
        else{
            if(audioFile.getName().length() < 25){
                audioLabel.setText(audioFile.getName());
            }
            else{
                audioLabel.setText(audioFile.getName().substring(0, 24) + "...");
            }
        }
    }

    public void bindCurrentTimeLabel(){
        currentTimeLabel.textProperty().bind(Bindings.createStringBinding(new Callable<String>() {
            @Override
            public String call() throws Exception {

                // Check where the time slider is.
                if(timeSlider.getValue() == timeSlider.getMax()){
                    return getTime(mediaPlayer.getTotalDuration()) + " / ";
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

    public void labelMatchEndSong(String labelTime, String labelTotalTime){
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
    }

    AudioTrackCoordinates getTrackCoordinates(){
        return (AudioTrackCoordinates) trackCoordinates;
    }

    private void setTrackAudio(File audioFile){
        this.audioFile = audioFile;
        media = new Media(this.audioFile.toURI().toString());
        mediaPlayer = new MediaPlayer(media);
        audioLabel.setText(audioFile.getName());
    }

    private File getNewAudioFile(){
        // Open the file browser at the current directory.
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File("."));
        fileChooser.setTitle("Select a file for track " + trackNumber + "...");
        return fileChooser.showOpenDialog(new Stage());
    }

    private void addNewAudioFile(File newFile){
        setTrackAudio(newFile);

        // Bidirectionally bind volume slider value to volume property of media player.
        mediaPlayer.volumeProperty().bindBidirectional(volumeSlider.valueProperty());
        bindCurrentTimeLabel();

        // Add listeners.
        AudioTrackListeners.addUnstableListeners(AudioTrack.this);
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

        // Refresh listeners.
        AudioTrackListeners.removeUnstableListeners(AudioTrack.this);
        AudioTrackListeners.addUnstableListeners(AudioTrack.this);

        // Set default values.
        setStateAfterFileChange();
    }

    void updateFile(){
        File selectedFile = getNewAudioFile();
        if(selectedFile == null) return;

        // Unsync if needed.
        boolean synced = false;
        if(masterTrack.synced){
            masterTrack.syncButton.fire();
            synced = true;
        }

        if(trackHasFile()){
            changeAudioFile(selectedFile);
        }
        else {
            addNewAudioFile(selectedFile);
        }

        // Resync if needed.
        if(synced){
            masterTrack.timeSlider.setValue(0.0);
            masterTrack.syncButton.fire();
        }

        masterTrack.PPRButton.setDisable(false);
        masterTrack.syncButton.setDisable(false);
        masterTrack.timeSlider.setDisable(false);
        if(!masterTrack.synced){
            PPRButton.setDisable(false);
            timeSlider.setDisable(false);
            volumeSlider.setDisable(false);
        }
    }

    /**
     * Focuses this audio track.
     */
    void focusTrack(){
        for(AudioTrack track: masterTrack.audioTracks){
            if(masterTrack.synced){
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
        masterTrack.setSwitchDisabled();
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

    void undoFocus(){
        focused = false;
        trackLabel.borderProperty().set(null);
        for(AudioTrack track: masterTrack.audioTracks){
            if(track.trackNumber != trackNumber){
                // Bind the volume slider of all non-focused tracks.
                if(masterTrack.synced && track.trackHasFile()){
                    MasterTrackListeners.bindSliderValueProperties(track.volumeSlider, masterTrack.volumeSlider);
                }
                track.volumeSlider.setValue(1.0);
            }
        }
        masterTrack.setSwitchDisabled();
    }

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

    boolean trackHasFile(){
        return (audioFile != null) && (media != null) && (mediaPlayer != null);
    }

    void pprOnAction(){
        if(!trackHasFile()) return;
        bindCurrentTimeLabel();
        if(atEndOfMedia){
            // System.out.println("end of media " + trackNumber);
            PPRButton.setText("Pause");
            timeSlider.setValue(0.0);
            atEndOfMedia = false;
            isPlaying = false;
            pauseTime = 0.0; // Update pause time.
        }
        if(isPlaying){
            // System.out.println("playing " + trackNumber);
            PPRButton.setText("Play");
            mediaPlayer.pause();
            isPlaying = false;
            pauseTime = mediaPlayer.getCurrentTime().toSeconds(); // Update pause time.
        }
        else{
            // System.out.println("paused " + trackNumber);
            mediaPlayer.seek(Duration.seconds(pauseTime));
            PPRButton.setText("Pause");
            mediaPlayer.play();
            isPlaying = true;
        }
    }
}
