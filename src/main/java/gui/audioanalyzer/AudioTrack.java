package gui.audioanalyzer;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
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
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.io.File;
import java.util.concurrent.Callable;

public class AudioTrack extends Track{

    File audioFile;
    Media media;
    MediaPlayer mediaPlayer;

    boolean atEndOfMedia = false;
    boolean isPlaying = false;
    private boolean isMuted = false;
    double pauseTime;
    private MasterTrack masterTrack; // The master track that controls this audio track.

    // JavaFX objects.
    @FXML
    Separator upperSeparator;
    @FXML
    Label audioLabel;

    // Listeners.
    private final EventHandler<ActionEvent> pprButtonOnActionEH = new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent actionEvent) {
            bindCurrentTimeLabel();
            Button buttonPlay = (Button) actionEvent.getSource();
            if(atEndOfMedia){
                timeSlider.setValue(0.0);
                atEndOfMedia = false;
                isPlaying = false;
                pauseTime = 0.0; // Update pause time.
            }
            if(isPlaying){
//                    buttonPlay.setGraphic(ivPlay);
                buttonPlay.setText("Play");
                mediaPlayer.pause();
                isPlaying = false;
                pauseTime = mediaPlayer.getCurrentTime().toSeconds(); // Update pause time.
            }
            else{
                mediaPlayer.seek(Duration.seconds(pauseTime));
//                    buttonPlay.setGraphic(ivPause);
                buttonPlay.setText("Pause");
                mediaPlayer.play();
                isPlaying = true;
            }
        }
    };

    private final ChangeListener<String> pprButtonTextPropertyCL = new ChangeListener<String>() {
        @Override
        public void changed(ObservableValue<? extends String> observableValue, String oldValue, String newValue) {
            if(!masterTrack.synced){
                if(masterTrack.PPRButton.getText().equals("Play")){
                    if(newValue.equals("Pause")){
                        boolean allTracksPlaying = true;
                        for(AudioTrack track: masterTrack.audioTracks){
                            if(!track.PPRButton.getText().equals("Pause")){
                                allTracksPlaying = false;
                            }
                        }
                        if(allTracksPlaying){
                            masterTrack.PPRButton.setText("Pause");
                        }
                    }
                }
                else if(masterTrack.PPRButton.getText().equals("Pause")){
                    if(newValue.equals("Play")){
                        boolean allTracksPaused = true;
                        for(AudioTrack track: masterTrack.audioTracks){
                            if(!track.PPRButton.getText().equals("Play")){
                                allTracksPaused = false;
                            }
                        }
                        if(allTracksPaused){
                            masterTrack.PPRButton.setText("Play");
                        }
                    }
                }
            }
        }
    };

    private final InvalidationListener volumeSliderValuePropertyIL = new InvalidationListener() {
        @Override
        public void invalidated(Observable observable) {
            mediaPlayer.setVolume(volumeSlider.getValue());
            if(mediaPlayer.getVolume() != 0.0){
                isMuted = false;
            }
            else{
                isMuted = true;
            }
        }
    };

    private final ChangeListener<Duration> mediaPlayerTotalDurationCL = new ChangeListener<Duration>() {
        @Override
        public void changed(ObservableValue<? extends Duration> observableValue, Duration oldDuration, Duration newDuration) {
            bindCurrentTimeLabel();
            timeSlider.setMax(newDuration.toSeconds());
            totalTimeLabel.setText(getTime(newDuration));

            // Update the longest track in masterTrack.
            if(masterTrack.longestAudioTrack == null || mediaPlayer.getTotalDuration().toSeconds() > masterTrack.longestAudioTrack.mediaPlayer.getTotalDuration().toSeconds()){
                masterTrack.longestAudioTrack = AudioTrack.this;

                // Update master track length even if not synced.
                masterTrack.bindSliderMaxValueProperties(masterTrack.timeSlider, timeSlider);
                masterTrack.bindLabelValueProperties(masterTrack.totalTimeLabel, totalTimeLabel);
                masterTrack.bindLabelValueProperties(masterTrack.currentTimeLabel, currentTimeLabel);
            }
        }
    };

    private final ChangeListener<Boolean> timeSliderValueChangingCL = new ChangeListener<Boolean>() {
        @Override
        public void changed(ObservableValue<? extends Boolean> observableValue, Boolean wasChanging, Boolean isChanging) {
            bindCurrentTimeLabel();
            if(!isChanging){
                mediaPlayer.seek(Duration.seconds(timeSlider.getValue()));
            }
        }
    };

    private final ChangeListener<Number> timeSliderValueCL = new ChangeListener<Number>() {
        @Override
        public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) {
            bindCurrentTimeLabel();
            double currentTime = mediaPlayer.getCurrentTime().toSeconds();
            if(Math.abs(currentTime - newValue.doubleValue()) > 0.5){
                mediaPlayer.seek(Duration.seconds(newValue.doubleValue()));
            }
            labelMatchEndSong(currentTimeLabel.getText(), totalTimeLabel.getText());
        }
    };

    private final ChangeListener<Duration> mediaPlayerCurrentTimeCL =  new ChangeListener<Duration>() {
        @Override
        public void changed(ObservableValue<? extends Duration> observableValue, Duration oldTime, Duration newTime) {
            bindCurrentTimeLabel();
            if(!timeSlider.isValueChanging()){
                timeSlider.setValue(newTime.toSeconds());
            }
            labelMatchEndSong(currentTimeLabel.getText(), totalTimeLabel.getText());
        }
    };

    private final Runnable mediaPlayerOnEndOfMediaR = new Runnable() {
        @Override
        public void run() {
            //                PPRButton.setGraphic(ivRestart);
            PPRButton.setText("Restart");
            atEndOfMedia = true;
            if(!currentTimeLabel.textProperty().equals(totalTimeLabel.textProperty())){
                currentTimeLabel.textProperty().unbind();
                currentTimeLabel.setText(getTime(mediaPlayer.getTotalDuration()) + " / ");
            }
        }
    };

    private final EventHandler<MouseEvent> timeSliderOnMouseClickedEH = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
            bindCurrentTimeLabel();
            pauseTime = mediaPlayer.getCurrentTime().toSeconds(); // Update pause time.
        }
    };

    private final EventHandler<MouseEvent> timeSliderOnMouseReleasedEH = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
            // Un-mute audio after scrubbing.
            mediaPlayer.setMute(false);
            isMuted = false;
            pauseTime = mediaPlayer.getCurrentTime().toSeconds(); // Update pause time.

            // Update time label to fix sluggish time bug.
        }
    };

    private final EventHandler<MouseEvent> timeSliderOnDragDetectedEH = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
            // Mute audio if scrubbing.
            mediaPlayer.setMute(true);
            isMuted = true;
        }
    };

    public AudioTrack(int trackNumber, AudioTrackCoordinates coordinates, MasterTrack masterTrack){
        this.masterTrack = masterTrack;
        this.trackNumber = trackNumber;
        trackCoordinates = coordinates;

        upperSeparator = new Separator();
        initializeTrackObject(upperSeparator, 0.0, getTrackCoordinates().upperSeparatorY, SEPARATOR_WIDTH, SEPARATOR_HEIGHT);

        trackLabel = new Label("Track " + this.trackNumber);
        initializeTrackObject(trackLabel, getTrackCoordinates().trackLabelX, getTrackCoordinates().trackLabelY, TRACK_LABEL_WIDTH, LABEL_HEIGHT);

        audioLabel = new Label("song title " + this.trackNumber);
        initializeTrackObject(audioLabel, getTrackCoordinates().audioLabelX, getTrackCoordinates().audioLabelY, AUDIO_LABEL_WIDTH, LABEL_HEIGHT);

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

        timeSlider = new Slider();
        initializeTrackObject(timeSlider, getTrackCoordinates().timeSliderX, getTrackCoordinates().timeSliderY, TIME_SLIDER_WIDTH, SLIDER_HEIGHT);

        currentTimeLabel = new Label();
        initializeTrackObject(currentTimeLabel, getTrackCoordinates().currentTimeLabelX, getTrackCoordinates().currentTimeLabelY, CURRENT_TIME_LABEL_WIDTH, LABEL_HEIGHT);

        totalTimeLabel = new Label();
        initializeTrackObject(totalTimeLabel, getTrackCoordinates().totalTimeLabelX, getTrackCoordinates().totalTimeLabelY, TOTAL_TIME_LABEL_WIDTH, LABEL_HEIGHT);

        initializeTrack();
    }

    @Override
    void initializeTrack(){
        // Load media.
        audioFile = new File("src/test_audio_files/Arctic Expedition (Instrumental) Mix 5.wav"); // For testing.
        if(audioFile.getName().length() < 25){
            audioLabel.setText(audioFile.getName());
        }
        else{
            audioLabel.setText(audioFile.getName().substring(0, 24) + "...");
        }
        media = new Media(audioFile.toURI().toString());
        mediaPlayer = new MediaPlayer(media);
        PPRButton.setText("Play");
        PPRButton.setOnAction(pprButtonOnActionEH);

        // Bidirectionally bind volume slider value to volume property of media player.
        mediaPlayer.volumeProperty().bindBidirectional(volumeSlider.valueProperty());
        bindCurrentTimeLabel();

        // Add listeners.

        // This is needed to ensure that when the PPR button is pressed, the appropriate change is made to the master track PPR button text.
        // This does not need to be used when the tracks are synced because bindings produce the desired behaviour in that case.
        PPRButton.textProperty().addListener(pprButtonTextPropertyCL);

        volumeSlider.valueProperty().addListener(volumeSliderValuePropertyIL);
        mediaPlayer.totalDurationProperty().addListener(mediaPlayerTotalDurationCL);
        timeSlider.valueChangingProperty().addListener(timeSliderValueChangingCL);
        timeSlider.valueProperty().addListener(timeSliderValueCL);
        mediaPlayer.currentTimeProperty().addListener(mediaPlayerCurrentTimeCL);
        mediaPlayer.setOnEndOfMedia(mediaPlayerOnEndOfMediaR);
        timeSlider.setOnMouseClicked(timeSliderOnMouseClickedEH);
        timeSlider.setOnMouseReleased(timeSliderOnMouseReleasedEH);
        timeSlider.setOnDragDetected(timeSliderOnDragDetectedEH);
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
//                    PPRButton.setGraphic(ivPause);
                    PPRButton.setText("Pause");
                }
                else{
//                    PPRButton.setGraphic(ivPlay);
                    PPRButton.setText("Play");
                }
                return;
            }
        }
        atEndOfMedia = true;
//        PPRButton.setGraphic(ivRestart);
        PPRButton.setText("Restart");
    }

    AudioTrackCoordinates getTrackCoordinates(){
        return (AudioTrackCoordinates) trackCoordinates;
    }

    private void selectFile(){
        // Pause all tracks.
        if(masterTrack.PPRButton.getText().equals("Pause")){
            masterTrack.PPRButton.fire();
        }
        if(masterTrack.PPRButton.getText().equals("Restart")){
            masterTrack.PPRButton.fire();
            masterTrack.PPRButton.fire();
        }
        if(PPRButton.getText().equals("Pause")){
            PPRButton.fire();
        }
        if(PPRButton.getText().equals("Restart")){
            PPRButton.fire();
            PPRButton.fire();
        }
        // Unsync tracks.
        if(masterTrack.synced){
            masterTrack.syncButton.fire();
        }

        // Open the file browser at the current directory.
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File("."));
        fileChooser.setTitle("Select a file for track " + trackNumber + "...");
        File selectedFile = fileChooser.showOpenDialog(new Stage());
        if(selectedFile != null){
            media = new Media(selectedFile.toURI().toString());
        }
    }
}
