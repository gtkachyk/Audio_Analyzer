package gui.audioanalyzer;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import java.io.File;
import java.util.concurrent.Callable;

public class AudioTrack extends Track{

    File audioFile;
    Media media;
    MediaPlayer mediaPlayer;

    private boolean atEndOfMedia = false;
    private boolean isPlaying = false;
    private boolean isMuted = false;
    private double pauseTime;

    // JavaFX objects.
    @FXML
    private Separator upperSeparator;
    @FXML
    private Label audioLabel;

    public AudioTrack(int trackNumber, AudioTrackCoordinates coordinates, AnchorPane anchorPane){
        this.trackNumber = trackNumber;
        trackCoordinates = coordinates;

        ObservableList<Node> anchorPaneChildren = anchorPane.getChildren();

        upperSeparator = new Separator();
        initializeTrackObject(upperSeparator, 0.0, getTrackCoordinates().upperSeparatorY, SEPARATOR_WIDTH, SEPARATOR_HEIGHT);
        anchorPaneChildren.add(upperSeparator);

        trackLabel = new Label("Track " + this.trackNumber);
        initializeTrackObject(trackLabel, getTrackCoordinates().trackLabelX, getTrackCoordinates().trackLabelY, TRACK_LABEL_WIDTH, LABEL_HEIGHT);
        anchorPaneChildren.add(trackLabel);

        audioLabel = new Label("song title " + this.trackNumber);
        initializeTrackObject(audioLabel, getTrackCoordinates().audioLabelX, getTrackCoordinates().audioLabelY, AUDIO_LABEL_WIDTH, LABEL_HEIGHT);
        anchorPaneChildren.add(audioLabel);

        lowerVolumeLabel = new Label("-");
        initializeTrackObject(lowerVolumeLabel, getTrackCoordinates().lowerVolumeLabelX, getTrackCoordinates().lowerVolumeLabelY, LOWER_VOLUME_LABEL_WIDTH, LABEL_HEIGHT);
        anchorPaneChildren.add(lowerVolumeLabel);

        volumeSlider = new Slider();
        initializeTrackObject(volumeSlider, getTrackCoordinates().volumeSliderX, getTrackCoordinates().volumeSliderY, VOLUME_SLIDER_WIDTH, SLIDER_HEIGHT);
        volumeSlider.setValue(volumeSlider.getMax());
        anchorPaneChildren.add(volumeSlider);

        raiseVolumeLabel = new Label("+");
        initializeTrackObject(raiseVolumeLabel, getTrackCoordinates().raiseVolumeLabelX, getTrackCoordinates().raiseVolumeLabelY, RAISE_VOLUME_LABEL_WIDTH, LABEL_HEIGHT);
        anchorPaneChildren.add(raiseVolumeLabel);

        PPRButton = new Button();
        initializeTrackObject(PPRButton, getTrackCoordinates().PPRButtonX, getTrackCoordinates().PPRButtonY, PPR_BUTTON_WIDTH, PPR_BUTTON_HEIGHT);
        anchorPaneChildren.add(PPRButton);

        timeSlider = new Slider();
        initializeTrackObject(timeSlider, getTrackCoordinates().timeSliderX, getTrackCoordinates().timeSliderY, TIME_SLIDER_WIDTH, SLIDER_HEIGHT);
        anchorPaneChildren.add(timeSlider);

        currentTimeLabel = new Label();
        initializeTrackObject(currentTimeLabel, getTrackCoordinates().currentTimeLabelX, getTrackCoordinates().currentTimeLabelY, CURRENT_TIME_LABEL_WIDTH, LABEL_HEIGHT);
        anchorPaneChildren.add(currentTimeLabel);

        totalTimeLabel = new Label();
        initializeTrackObject(totalTimeLabel, getTrackCoordinates().totalTimeLabelX, getTrackCoordinates().totalTimeLabelY, TOTAL_TIME_LABEL_WIDTH, LABEL_HEIGHT);
        anchorPaneChildren.add(totalTimeLabel);

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

        // Load button images.
//        final int IV_SIZE = 15;
//        Image imagePlay = new Image(new File("src/images/play_button.png").toURI().toString());
//        ivPlay = new ImageView(imagePlay);
//        ivPlay.setFitHeight(IV_SIZE);
//        ivPlay.setFitWidth(IV_SIZE);
//
//        Image imagePause = new Image(new File("src/images/pause_button.jpg").toURI().toString());
//        ivPause = new ImageView(imagePause);
//        ivPause.setFitHeight(IV_SIZE);
//        ivPause.setFitWidth(IV_SIZE);
//
//        Image imageRestart = new Image(new File("src/images/restart_button.jpg").toURI().toString());
//        ivRestart = new ImageView(imageRestart);
//        ivRestart.setFitHeight(IV_SIZE);
//        ivRestart.setFitWidth(IV_SIZE);

        // Set initial button images.
//        PPRButton.setGraphic(ivPlay);
        PPRButton.setText("Play");

        PPRButton.setOnAction(new EventHandler<ActionEvent>() {
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
        });

        // Bidirectionally bind volume slider value to volume property of media player.
        mediaPlayer.volumeProperty().bindBidirectional(volumeSlider.valueProperty());
        bindCurrentTimeLabel();

        // Add listeners.
        volumeSlider.valueProperty().addListener(new InvalidationListener() {
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
        });

        mediaPlayer.totalDurationProperty().addListener(new ChangeListener<Duration>() {
            @Override
            public void changed(ObservableValue<? extends Duration> observableValue, Duration oldDuration, Duration newDuration) {
                bindCurrentTimeLabel();
                timeSlider.setMax(newDuration.toSeconds());
                totalTimeLabel.setText(getTime(newDuration));
            }
        });

        timeSlider.valueChangingProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean wasChanging, Boolean isChanging) {
                bindCurrentTimeLabel();
                if(!isChanging){
                    mediaPlayer.seek(Duration.seconds(timeSlider.getValue()));
                }
            }
        });

        timeSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) {
                bindCurrentTimeLabel();
                double currentTime = mediaPlayer.getCurrentTime().toSeconds();
                if(Math.abs(currentTime - newValue.doubleValue()) > 0.5){
                    mediaPlayer.seek(Duration.seconds(newValue.doubleValue()));
                }
                labelMatchEndSong(currentTimeLabel.getText(), totalTimeLabel.getText());
            }
        });

        mediaPlayer.currentTimeProperty().addListener(new ChangeListener<Duration>() {
            @Override
            public void changed(ObservableValue<? extends Duration> observableValue, Duration oldTIme, Duration newTime) {
                bindCurrentTimeLabel();
                if(!timeSlider.isValueChanging()){
                    timeSlider.setValue(newTime.toSeconds());
                }
                labelMatchEndSong(currentTimeLabel.getText(), totalTimeLabel.getText());
            }
        });

        mediaPlayer.setOnEndOfMedia(new Runnable() {
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
        });

        timeSlider.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                bindCurrentTimeLabel();
                pauseTime = mediaPlayer.getCurrentTime().toSeconds(); // Update pause time.
            }
        });

        timeSlider.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                // Un-mute audio after scrubbing.
                mediaPlayer.setMute(false);
                isMuted = false;

                // Update time label.
            }
        });

        timeSlider.setOnDragDetected(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                // Mute audio if scrubbing.
                mediaPlayer.setMute(true);
                isMuted = true;
            }
        });
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
}
