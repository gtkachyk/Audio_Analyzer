package gui.audioanalyzer;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public abstract class Track {

    // Constants.
    static final double LABEL_HEIGHT = 17.6;
    static final double SLIDER_HEIGHT = 14.4;
    static final double SEPARATOR_WIDTH = 444.0;
    static final double SEPARATOR_HEIGHT = 14.4;
    static final double TRACK_LABEL_WIDTH = 38.4;
    static final double AUDIO_LABEL_WIDTH = 76.0;
    static final double LOWER_VOLUME_LABEL_WIDTH = 4.8;
    static final double VOLUME_SLIDER_WIDTH = 100.0;
    static final double RAISE_VOLUME_LABEL_WIDTH = 8.8;
    static final double PPR_BUTTON_WIDTH = 76.0;
    static final double PPR_BUTTON_HEIGHT = 26.4;
    static final double TIME_SLIDER_WIDTH = 232.0;
    static final double CURRENT_TIME_LABEL_WIDTH = 43.6;
    static final double TOTAL_TIME_LABEL_WIDTH = 32.4;

    int trackNumber;
    TrackCoordinates trackCoordinates;
    // AudioTrackCoordinates audioTrackCoordinates;

    ImageView ivPlay;
    ImageView ivPause;
    ImageView ivRestart;

    // JavaFX objects.
    @FXML
    Label trackLabel;
    @FXML
    Label lowerVolumeLabel;
    @FXML
    Slider volumeSlider;
    @FXML
    Label raiseVolumeLabel;
    @FXML
    Button PPRButton;
    @FXML
    Slider timeSlider;
    @FXML
    Label currentTimeLabel;
    @FXML
    Label totalTimeLabel;

    /**
     *
     * @param trackObject
     * @param xCoordinate
     * @param yCoordinate
     * @param width
     * @param height
     */
    void initializeTrackObject(Object trackObject, double xCoordinate, double yCoordinate, double width, double height){
        Class<?> classObject = trackObject.getClass();
        try{
            Method setLayoutX = classObject.getMethod("setLayoutX", double.class);
            setLayoutX.invoke(trackObject, xCoordinate);

            Method setLayoutY = classObject.getMethod("setLayoutY", double.class);
            setLayoutY.invoke(trackObject, yCoordinate);

            Method setPrefWidth = classObject.getMethod("setPrefWidth", double.class);
            setPrefWidth.invoke(trackObject, width);

            Method setPrefHeight = classObject.getMethod("setPrefHeight", double.class);
            setPrefHeight.invoke(trackObject, height);
        }
        catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e){
            e.printStackTrace();
        }
    }

    abstract void initializeTrack();

    String getTime(Duration time){
        int hours = (int) time.toHours();
        int minutes = (int) time.toMinutes();
        int seconds = (int) time.toSeconds();

        if(seconds > 59) seconds = seconds % 60;
        if(minutes > 59) minutes = minutes % 60;
        if(hours > 59) hours = hours % 60;

        if(hours > 0){
            // System.out.printf("getTime(%f) returned %s.%n", time.toSeconds(), String.format("%d:%02d:%02d", hours, minutes, seconds));
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        }
        else{
            // System.out.printf("getTime(%f) returned %s.%n", time.toSeconds(), String.format("%02d:%02d", minutes, seconds));
            return String.format("%02d:%02d", minutes, seconds);
        }
    }

    abstract TrackCoordinates getTrackCoordinates();
}
