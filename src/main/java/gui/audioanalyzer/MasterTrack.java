package gui.audioanalyzer;

import javafx.collections.ObservableList;
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
                    for(AudioTrack track: MainController.audioTracks){
//                        timeSlider.valueProperty().unbind();
                        track.timeSlider.valueProperty().unbind();
                    }
                }
                else{
                    syncButton.setText("Unlock");
                    synced = true;
                    for(AudioTrack track: MainController.audioTracks){
                        bindSliderValueProperties(timeSlider, track.timeSlider);
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

    public void bindLabelValueProperties(Label labelOne, Label labelTwo){
        labelOne.textProperty().bind(labelTwo.textProperty());
    }

    // TODO: Bind labels.
}