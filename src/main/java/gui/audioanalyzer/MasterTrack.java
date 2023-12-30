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
import javafx.util.Duration;
import java.util.ArrayList;

public class MasterTrack extends Track{

    // Constants.
    private static final double ADD_TRACK_BUTTON_WIDTH = 69.6;
    private static final double ADD_TRACK_BUTTON_HEIGHT = 25.6;
    private static final double MILLISECONDS_PER_SECOND = 1000.0;
    private static final int MAX_TRACKS = 10;

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
    boolean synced = true;
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

    ArrayList<AudioTrack> audioTracks = new ArrayList<>();
    AudioTrack longestAudioTrack = null;
    int numberOfAudioTracks = 0;

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

        timeSlider = new Slider();
        initializeTrackObject(timeSlider, getTrackCoordinates().timeSliderX, getTrackCoordinates().timeSliderY, TIME_SLIDER_WIDTH, SLIDER_HEIGHT);

        currentTimeLabel = new Label("00:00 / ");
        initializeTrackObject(currentTimeLabel, getTrackCoordinates().currentTimeLabelX, getTrackCoordinates().currentTimeLabelY, CURRENT_TIME_LABEL_WIDTH, LABEL_HEIGHT);

        totalTimeLabel = new Label("00:00");
        initializeTrackObject(totalTimeLabel, getTrackCoordinates().totalTimeLabelX, getTrackCoordinates().totalTimeLabelY, TOTAL_TIME_LABEL_WIDTH, LABEL_HEIGHT);

        switchButton = new Button("Focus");
        initializeTrackObject(switchButton, getTrackCoordinates().switchButtonX, getTrackCoordinates().switchButtonY, PPR_BUTTON_WIDTH, PPR_BUTTON_HEIGHT);

        syncButton = new Button("Unlock");
        initializeTrackObject(syncButton, getTrackCoordinates().syncButtonX, getTrackCoordinates().syncButtonY, PPR_BUTTON_WIDTH, PPR_BUTTON_HEIGHT);

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
        PPRButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                if(PPRButton.getText().equals("Pause")){
                    PPRButton.setText("Play");
                    for(AudioTrack track: audioTracks){
                        // Pause all playing tracks.
                        if(track.isPlaying){
                            track.pprOnAction();
//                            track.PPRButton.fire();
                            if(track.atEndOfMedia){
                                track.pprOnAction();
//                                track.PPRButton.fire();
                            }
                        }
                    }
                }
                else if(PPRButton.getText().equals("Play")){
                    PPRButton.setText("Pause");
                    for(AudioTrack track: audioTracks){
                        // Play all paused tracks.
                        if(!track.isPlaying){
                            track.pprOnAction();
//                            track.PPRButton.fire();
                        }
                    }
                }
                else if(PPRButton.getText().equals("Restart")){
                    // TODO: Fix bug: tracks do not restart when PPRButton reads 'Restart'.
                }
            }
        });
    }

    MasterTrackCoordinates getTrackCoordinates(){
        return (MasterTrackCoordinates) trackCoordinates;
    }

    /**
     * Binds the valueProperty of two sliders to each other. Binding is bidirectional.
     * @param sliderOne The first slider to bind.
     * @param sliderTwo The second slider to bind.
     */
    public void bindSliderValueProperties(Slider sliderOne, Slider sliderTwo) {
        sliderOne.valueProperty().bindBidirectional(sliderTwo.valueProperty());
    }

    /**
     * Binds the maxProperty of sliderOne to sliderTwo. Binding is unidirectional.
     * @param sliderOne The slider whose maxProperty will be bound to sliderTwo.
     * @param sliderTwo The slider whose maxProperty will not be bound.
     */
    public void bindSliderMaxValueProperties(Slider sliderOne, Slider sliderTwo){
        sliderOne.maxProperty().bind(sliderTwo.maxProperty());
    }

    /**
     * Binds the onMouseClicked property of two sliders to each other. Binding is bidirectional.
     * @param sliderOne The first slider to bind.
     * @param sliderTwo The second slider to bind.
     */
    public void bindSliderOnMouseClickedProperty(Slider sliderOne, Slider sliderTwo){
        sliderOne.onMouseClickedProperty().bindBidirectional(sliderTwo.onMouseClickedProperty());
    }

    /**
     * Binds the onDragDetected property of two sliders to each other. Binding is bidirectional.
     * @param sliderOne The first slider to bind.
     * @param sliderTwo The second slider to bind.
     */
    public void bindSliderOnDragDetectedProperty(Slider sliderOne, Slider sliderTwo){
        sliderOne.onDragDetectedProperty().bindBidirectional(sliderTwo.onDragDetectedProperty());
    }

    /**
     * Binds the onMouseReleased property of two sliders to each other. Binding is bidirectional.
     * @param sliderOne The first slider to bind.
     * @param sliderTwo The second slider to bind.
     */
    public void bindSliderOnMouseReleasedProperty(Slider sliderOne, Slider sliderTwo){
        sliderOne.onMouseReleasedProperty().bindBidirectional(sliderTwo.onMouseReleasedProperty());
    }

    /**
     * Binds the textProperty of labelOne to labelTwo. Binding is unidirectional.
     * @param labelOne The label whose textProperty will be bound to labelTwo.
     * @param labelTwo The label whose textProperty will not be bound.
     */
    public void bindLabelValueProperties(Label labelOne, Label labelTwo){
        labelOne.textProperty().bind(labelTwo.textProperty());
    }

    /**
     * Binds the textProperty of buttonOne to buttonTwo. Binding is bidirectional.
     * @param buttonOne The button whose textProperty will be bound to buttonTwo.
     * @param buttonTwo The button whose textProperty will not be bound.
     */
    public void bindButtonTextProperties(Button buttonOne, Button buttonTwo){
        buttonTwo.textProperty().bindBidirectional(buttonOne.textProperty());
    }

    /**
     * Binds properties of this master track and all AudioTracks needed to synchronize them.
     */
    void sync(){
        // Remove the listener from the time slider value property.
        timeSlider.valueProperty().removeListener(timeSliderChangeListener);

        // Create unidirectional bindings.
        bindSliderMaxValueProperties(timeSlider, longestAudioTrack.timeSlider);
        bindLabelValueProperties(totalTimeLabel, longestAudioTrack.totalTimeLabel);
        bindLabelValueProperties(currentTimeLabel, longestAudioTrack.currentTimeLabel);

        // Create bidirectional bindings.
        for(AudioTrack track: audioTracks){
            track.pauseTime = timeSlider.getValue(); // Update pause time so all tracks resume from the position of the master track time slider.
            bindSliderValueProperties(track.timeSlider, timeSlider);
            bindSliderValueProperties(track.volumeSlider, volumeSlider); // Bind volumes.
            bindSliderOnMouseClickedProperty(timeSlider, track.timeSlider);
            bindSliderOnDragDetectedProperty(timeSlider, track.timeSlider);
            bindSliderOnMouseReleasedProperty(timeSlider, track.timeSlider);
            bindButtonTextProperties(PPRButton, track.PPRButton);

            track.PPRButton.setDisable(true);
            track.timeSlider.setDisable(true);
            track.volumeSlider.setDisable(true);
        }
    }

    /**
     * Unbinds all bound properties of the master track.
     */
    public void unSync(){
        // Unbind unidirectional bindings.
        timeSlider.maxProperty().unbind();
        currentTimeLabel.textProperty().unbind();
        totalTimeLabel.textProperty().unbind();

        // Unbind bidirectional bindings.
        for(AudioTrack track: audioTracks){
            Bindings.unbindBidirectional(timeSlider.valueProperty(), track.timeSlider.valueProperty());
            Bindings.unbindBidirectional(track.timeSlider.onMouseClickedProperty(), timeSlider.onMouseClickedProperty());
            Bindings.unbindBidirectional(volumeSlider.valueProperty(), track.volumeSlider.valueProperty());
            Bindings.unbindBidirectional(track.timeSlider.onDragDetectedProperty(), timeSlider.onDragDetectedProperty());
            Bindings.unbindBidirectional(track.timeSlider.onMouseReleasedProperty(), timeSlider.onMouseReleasedProperty());
            Bindings.unbindBidirectional(PPRButton.textProperty(), track.PPRButton.textProperty());

            track.PPRButton.setDisable(false);
            track.timeSlider.setDisable(false);
            track.volumeSlider.setDisable(false);
        }

        // These properties linger if not set to null. Unbinding them alone does not remove them.
        timeSlider.onMouseClickedProperty().set(null);
        timeSlider.onMouseReleasedProperty().set(null);
        timeSlider.onDragDetectedProperty().set(null);

        // Add a listener to the time slider to update the current time label when unsynced.
        timeSlider.valueProperty().addListener(timeSliderChangeListener);
    }

    void pauseAllTracks(){
        for(AudioTrack track: audioTracks){
            if(track.PPRButton.getText().equals("Pause")){
                track.PPRButton.fire();
            }
            if(track.PPRButton.getText().equals("Restart")){
                track.PPRButton.fire();
                track.PPRButton.fire();
            }
        }
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
        controller.removeAudioTrack(track);
        audioTracks.remove(track);
        numberOfAudioTracks--;

        if(numberOfAudioTracks < MAX_TRACKS){
            addTrackButton.setDisable(false);
        }

        // Shift up tracks and update track numbers.
        if(numberOfAudioTracks > 0){
            for(AudioTrack audioTrack: audioTracks){
                if(audioTrack.trackNumber > removedTrackNumber){
                    audioTrack.shiftTrackUp();
                    audioTrack.trackNumber--;
                    audioTrack.trackLabel.setText("Track " + audioTrack.trackNumber);
                }
            }
        }
        controller.resizeStageForAudioTrackChange();

        for(Track audioTrack: audioTracks){
            if(audioTrack.trackNumber == audioTracks.size()){
                audioTrack.lowerSeparator.setVisible(false);
            }
            else{
                audioTrack.lowerSeparator.setVisible(true);
            }
        }
    }
}
