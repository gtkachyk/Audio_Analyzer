package gui.audioanalyzer;

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
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.util.Duration;

import java.io.File;

public class MasterTrack extends Track{

    // Constants.
    private static final double ADD_TRACK_BUTTON_WIDTH = 69.6;
    private static final double ADD_TRACK_BUTTON_HEIGHT = 25.6;

    // JavaFX objects.
    @FXML
    private Label focusTrackLabel;
    @FXML
    private Button switchButton;
    @FXML
    private Button syncButton;
    @FXML
    Button addTrackButton;

    // Other data.
    boolean synced = true;
    private final ChangeListener<Number> timeSliderChangeListener = new ChangeListener<Number>() {
        @Override
        public void changed(ObservableValue observableValue, Number oldValue, Number newValue) {
            currentTimeLabel.setText(getTime(new Duration(timeSlider.getValue() * 1000.0)) + " / ");
        }
    };

    public MasterTrack(MasterTrackCoordinates masterTrackCoordinates, AnchorPane anchorPane){
        trackNumber = 0;
        trackCoordinates = masterTrackCoordinates;

        ObservableList<Node> anchorPaneChildren = anchorPane.getChildren();

        trackLabel = new Label("Master");
        initializeTrackObject(trackLabel, getTrackCoordinates().trackLabelX, getTrackCoordinates().trackLabelY, TRACK_LABEL_WIDTH, LABEL_HEIGHT);
        anchorPaneChildren.add(trackLabel);

        focusTrackLabel = new Label("Focus Track: " + 1);
        initializeTrackObject(focusTrackLabel, getTrackCoordinates().focusTrackLabelX, getTrackCoordinates().focusTrackLabelY, AUDIO_LABEL_WIDTH, LABEL_HEIGHT);
        anchorPaneChildren.add(focusTrackLabel);

        lowerVolumeLabel = new Label("-");
        initializeTrackObject(lowerVolumeLabel, getTrackCoordinates().lowerVolumeLabelX, getTrackCoordinates().lowerVolumeLabelY, LOWER_VOLUME_LABEL_WIDTH, LABEL_HEIGHT);
        anchorPaneChildren.add(lowerVolumeLabel);

        volumeSlider = new Slider();
        initializeTrackObject(volumeSlider, getTrackCoordinates().volumeSliderX, getTrackCoordinates().volumeSliderY, VOLUME_SLIDER_WIDTH, SLIDER_HEIGHT);
        volumeSlider.setMax(VOLUME_SLIDER_MAX);
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

        currentTimeLabel = new Label("00:00 / ");
        initializeTrackObject(currentTimeLabel, getTrackCoordinates().currentTimeLabelX, getTrackCoordinates().currentTimeLabelY, CURRENT_TIME_LABEL_WIDTH, LABEL_HEIGHT);
        anchorPaneChildren.add(currentTimeLabel);

        totalTimeLabel = new Label("00:00");
        initializeTrackObject(totalTimeLabel, getTrackCoordinates().totalTimeLabelX, getTrackCoordinates().totalTimeLabelY, TOTAL_TIME_LABEL_WIDTH, LABEL_HEIGHT);
        anchorPaneChildren.add(totalTimeLabel);

        switchButton = new Button("Switch");
        initializeTrackObject(switchButton, getTrackCoordinates().switchButtonX, getTrackCoordinates().switchButtonY, PPR_BUTTON_WIDTH, PPR_BUTTON_HEIGHT);
        anchorPaneChildren.add(switchButton);

        syncButton = new Button("Unlock");
        initializeTrackObject(syncButton, getTrackCoordinates().syncButtonX, getTrackCoordinates().syncButtonY, PPR_BUTTON_WIDTH, PPR_BUTTON_HEIGHT);
        anchorPaneChildren.add(syncButton);

        addTrackButton = new Button("Add Track");
        initializeTrackObject(addTrackButton, getTrackCoordinates().addTrackButtonX, getTrackCoordinates().addTrackButtonY, ADD_TRACK_BUTTON_WIDTH, ADD_TRACK_BUTTON_HEIGHT);
        anchorPaneChildren.add(addTrackButton);

        initializeTrack();
    }

    @Override
    void initializeTrack() {
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

        // Add listeners.
        syncButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
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
            }
        });

        // The PPR button will play all tracks from their current position.
        // When not synced, the PPR button should read 'Pause' only if all audio tracks are playing.
        // And it should read 'Play' otherwise.
        // If it reads 'Play' it should force all tracks to play when pressed.
        // If it reads 'Pause' it should force all tracks to pause when pressed.
        PPRButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                if(PPRButton.getText().equals("Pause")){
                    PPRButton.setText("Play");
                    for(AudioTrack track: MainController.audioTracks){
                        // Pause all playing tracks.
                        if(track.isPlaying){
                            track.PPRButton.fire();
                            if(track.atEndOfMedia){
                                track.PPRButton.fire();
                            }
                        }
                    }
                }
                else if(PPRButton.getText().equals("Play")){
                    PPRButton.setText("Pause");
                    for(AudioTrack track: MainController.audioTracks){
                        // Play all paused tracks.
                        if(!track.isPlaying){
                            track.PPRButton.fire();
                        }
                    }
                }
            }
        });
    }

    MasterTrackCoordinates getTrackCoordinates(){
        return (MasterTrackCoordinates) trackCoordinates;
    }

    /**
     *
     * @param sliderOne
     * @param sliderTwo
     */
    public void bindSliderValueProperties(Slider sliderOne, Slider sliderTwo) {
        sliderTwo.valueProperty().bindBidirectional(sliderOne.valueProperty());
    }

    /**
     *
     * @param sliderOne The slider whose maxProperty will not be bound.
     * @param sliderTwo The slider whose maxProperty will be bound to sliderTwo.
     */
    public void bindSliderMaxValueProperties(Slider sliderOne, Slider sliderTwo){
        sliderOne.maxProperty().bind(sliderTwo.maxProperty());
    }

    public void bindOnMouseClickedProperty(Slider sliderOne, Slider sliderTwo){
        sliderOne.onMouseClickedProperty().bindBidirectional(sliderTwo.onMouseClickedProperty());
    }

    public void bindOnDragDetectedProperty(Slider sliderOne, Slider sliderTwo){
        sliderOne.onDragDetectedProperty().bindBidirectional(sliderTwo.onDragDetectedProperty());
    }

    public void bindOnMouseReleasedProperty(Slider sliderOne, Slider sliderTwo){
        sliderOne.onMouseReleasedProperty().bindBidirectional(sliderTwo.onMouseReleasedProperty());
    }

    public void bindLabelValueProperties(Label labelOne, Label labelTwo){
        labelOne.textProperty().bind(labelTwo.textProperty());
    }

    public void bindButtonTextProperties(Button buttonOne, Button buttonTwo){
        buttonTwo.textProperty().bindBidirectional(buttonOne.textProperty());
    }

    /**
     * Binds properties of this master track and all AudioTracks needed to synchronize them.
     */
    private void sync(){
        timeSlider.valueProperty().removeListener(timeSliderChangeListener);
        bindSliderMaxValueProperties(timeSlider, MainController.longestAudioTrack.timeSlider);
        bindLabelValueProperties(totalTimeLabel, MainController.longestAudioTrack.totalTimeLabel);
        bindLabelValueProperties(currentTimeLabel, MainController.longestAudioTrack.currentTimeLabel);
        for(AudioTrack track: MainController.audioTracks){
            track.pauseTime = timeSlider.getValue(); // Update pause time so all tracks resume from the position of the master track time slider.
            bindSliderValueProperties(timeSlider, track.timeSlider);
            bindSliderValueProperties(volumeSlider, track.volumeSlider); // Bind volumes.
            bindOnMouseClickedProperty(timeSlider, track.timeSlider);
            bindOnDragDetectedProperty(timeSlider, track.timeSlider);
            bindOnMouseReleasedProperty(timeSlider, track.timeSlider);
            bindButtonTextProperties(PPRButton, track.PPRButton);
        }
    }

    /**
     * Unbinds all bound properties of the master track.
     */
    public void unSync(){
        timeSlider.maxProperty().unbind();
        currentTimeLabel.textProperty().unbind();
        totalTimeLabel.textProperty().unbind();

        for(AudioTrack track: MainController.audioTracks){
            Bindings.unbindBidirectional(timeSlider.valueProperty(), track.timeSlider.valueProperty());
            Bindings.unbindBidirectional(track.timeSlider.onMouseClickedProperty(), timeSlider.onMouseClickedProperty());
            Bindings.unbindBidirectional(volumeSlider.valueProperty(), track.volumeSlider.valueProperty());
            Bindings.unbindBidirectional(track.timeSlider.onDragDetectedProperty(), timeSlider.onDragDetectedProperty());
            Bindings.unbindBidirectional(track.timeSlider.onMouseReleasedProperty(), timeSlider.onMouseReleasedProperty());
            Bindings.unbindBidirectional(PPRButton.textProperty(), track.PPRButton.textProperty());
        }

        timeSlider.onMouseClickedProperty().set(null);
        timeSlider.onMouseReleasedProperty().set(null);
        timeSlider.onDragDetectedProperty().set(null);

        timeSlider.valueProperty().addListener(timeSliderChangeListener);
    }
}
