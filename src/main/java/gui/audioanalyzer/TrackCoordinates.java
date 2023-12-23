package gui.audioanalyzer;

public abstract class TrackCoordinates {

    // Constants.
    static final double MASTER_TRACK_SEPARATOR_Y_COORDINATE = 163.0;
    static final double PPR_BUTTON_X_COORDINATE = 14.0;
    static final double TRACK_LABEL_X_COORDINATE = 32.0;
    static final double TIME_SLIDER_X_COORDINATE = 118.0;
    static final double LOWER_VOLUME_LABEL_X_COORDINATE = 236.0;
    static final double VOLUME_SLIDER_X_COORDINATE = 245.0;
    static final double RAISE_VOLUME_LABEL_X_COORDINATE = 348.0;
    static final double CURRENT_TIME_LABEL_X_COORDINATE = 357.0;
    static final double TOTAL_TIME_LABEL_X_COORDINATE = 394.0;

    // X Coordinates.
    double PPRButtonX;
    double trackLabelX;
    double timeSliderX;
    double lowerVolumeLabelX;
    double volumeSliderX;
    double raiseVolumeLabelX;
    double currentTimeLabelX;
    double totalTimeLabelX;

    // Y Coordinates.
    double trackLabelY, lowerVolumeLabelY, volumeSliderY, raiseVolumeLabelY;
    double PPRButtonY;
    double timeSliderY, currentTimeLabelY, totalTimeLabelY;


    abstract double calculateOffset(int trackNumber);
    abstract void setXCoordinates();
    abstract void setYCoordinates(double offset);
}
