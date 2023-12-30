package gui.audioanalyzer;

public class AudioTrackCoordinates extends TrackCoordinates{
    
    // Constants.
    static final double AUDIO_TRACK_HEIGHT = 91.0;
    static final double UPPER_SEPARATOR_X_COORDINATE = 0.0;
    static final double TRACK_ONE_ROW_ONE_Y_COORDINATE = 191.0; // The y coordinate of the first row of objects in the first track.
    static final double TRACK_ONE_ROW_TWO_Y_COORDINATE = 221.0; // Only the PPR button.
    static final double TRACK_ONE_ROW_THREE_Y_COORDINATE = 225.0;
    static final double AUDIO_LABEL_X_COORDINATE = 92.0;
    static final double REMOVE_TRACK_BUTTON_X_COORDINATE = 422.0; // Original value 400.0.

    // Other data.
    double upperSeparatorX;
    double audioLabelX;
    double removeTrackButtonX;

    double upperSeparatorY;
    double audioLabelY;
    double removeTrackButtonY;

    public AudioTrackCoordinates(int trackNumber){
        double offsetFromFirstTrack = calculateOffset(trackNumber);
        setXCoordinates();
        setYCoordinates(offsetFromFirstTrack);
    }

    double calculateOffset(int trackNumber){
        if(trackNumber == 1){
            return 0.0;
        }
        else{
            return (trackNumber - 1) * AUDIO_TRACK_HEIGHT; // (trackNumber - 1) is the number of tracks added by pressing the 'add track' button.
        }
    }

    void setXCoordinates(){
        upperSeparatorX = UPPER_SEPARATOR_X_COORDINATE;
        PPRButtonX = PPR_BUTTON_X_COORDINATE;
        trackLabelX = TRACK_LABEL_X_COORDINATE;
        audioLabelX = AUDIO_LABEL_X_COORDINATE;
        timeSliderX = TIME_SLIDER_X_COORDINATE;
        lowerVolumeLabelX = LOWER_VOLUME_LABEL_X_COORDINATE;
        volumeSliderX = VOLUME_SLIDER_X_COORDINATE;
        raiseVolumeLabelX = RAISE_VOLUME_LABEL_X_COORDINATE;
        currentTimeLabelX = CURRENT_TIME_LABEL_X_COORDINATE;
        totalTimeLabelX = TOTAL_TIME_LABEL_X_COORDINATE;
        removeTrackButtonX = REMOVE_TRACK_BUTTON_X_COORDINATE;
    }

    void setYCoordinates(double offset){
        upperSeparatorY = MASTER_TRACK_SEPARATOR_Y_COORDINATE + offset;
        trackLabelY = audioLabelY = lowerVolumeLabelY = volumeSliderY = raiseVolumeLabelY = TRACK_ONE_ROW_ONE_Y_COORDINATE + offset;
        PPRButtonY = TRACK_ONE_ROW_TWO_Y_COORDINATE + offset;
        timeSliderY = currentTimeLabelY = totalTimeLabelY = TRACK_ONE_ROW_THREE_Y_COORDINATE + offset;
        removeTrackButtonY = upperSeparatorY + 13.0; // Arbitrary offset.
    }
}
