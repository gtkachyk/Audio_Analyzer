package gui.audioanalyzer;

public class MasterTrackCoordinates extends TrackCoordinates{

    // Constants.
    static final double OFFSET_FROM_TOP = 30.0; // The distance between the top of the window and first row of elements in the master track.
    static final double ROW_ONE_AND_PPR_BUTTON_GAP = 26.0;
    static final double ROW_ONE_AND_TIME_SLIDER_GAP = 32.0;
    static final double ROW_ONE_AND_TIME_LABELS_GAP = 32.0;
    static final double ROW_ONE_AND_SWITCH_BUTTON_GAP = 61.0;
    static final double ROW_ONE_AND_BOTTOM_BUTTONS_GAP = 97.0;
    static final double FOCUS_TRACK_LABEL_X_COORDINATE = 92.0;
    static final double ADD_TRACK_BUTTON_X_COORDINATE = 357.0; // Original value 339.0.

    // X coordinates.
    double focusTrackLabelX;
    double switchButtonX;
    double syncButtonX;
    double addTrackButtonX;

    // Y coordinates.
    double focusTrackLabelY;
    double switchButtonY;
    double syncButtonY;
    double addTrackButtonY;

    public MasterTrackCoordinates(){
        setXCoordinates();
        setYCoordinates(OFFSET_FROM_TOP);
    }

    double calculateOffset(int trackNumber){
        return 0.0;
    }

    void setXCoordinates(){
        trackLabelX = TRACK_LABEL_X_COORDINATE;
        focusTrackLabelX = FOCUS_TRACK_LABEL_X_COORDINATE;
        PPRButtonX = PPR_BUTTON_X_COORDINATE;
        timeSliderX = TIME_SLIDER_X_COORDINATE;
        lowerVolumeLabelX = LOWER_VOLUME_LABEL_X_COORDINATE;
        volumeSliderX = VOLUME_SLIDER_X_COORDINATE;
        raiseVolumeLabelX = RAISE_VOLUME_LABEL_X_COORDINATE;
        currentTimeLabelX = CURRENT_TIME_LABEL_X_COORDINATE;
        totalTimeLabelX = TOTAL_TIME_LABEL_X_COORDINATE;
        switchButtonX = PPR_BUTTON_X_COORDINATE; // Add new constants for these later.
        syncButtonX = PPR_BUTTON_X_COORDINATE;
        addTrackButtonX = ADD_TRACK_BUTTON_X_COORDINATE;
    }

    void setYCoordinates(double offset){
        trackLabelY = focusTrackLabelY = lowerVolumeLabelY = volumeSliderY = raiseVolumeLabelY = offset;
        PPRButtonY = offset + ROW_ONE_AND_PPR_BUTTON_GAP;
        timeSliderY = offset + ROW_ONE_AND_TIME_SLIDER_GAP;
        currentTimeLabelY = totalTimeLabelY = offset + ROW_ONE_AND_TIME_LABELS_GAP;
        switchButtonY = offset + ROW_ONE_AND_SWITCH_BUTTON_GAP;
        syncButtonY = addTrackButtonY = offset + ROW_ONE_AND_BOTTOM_BUTTONS_GAP;
    }
}
