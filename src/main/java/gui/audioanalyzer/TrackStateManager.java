package gui.audioanalyzer;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;

import java.util.ArrayList;

public class TrackStateManager {

    static void refreshTrackNumbers(MasterTrack masterTrack){
        for(int i = 0; i < masterTrack.audioTracks.size(); i++){
            AudioTrack track = masterTrack.audioTracks.get(i);
            track.trackNumber = i + 1;
            track.trackLabel.setText("Track " + track.trackNumber);
        }
    }

    static void refreshLongestAudioTrack(MasterTrack masterTrack){
        if(masterTrack.audioTracksSortedByDuration.size() > 0){
            masterTrack.bubbleSortAudioTracksByDuration(masterTrack.audioTracksSortedByDuration);
            masterTrack.longestAudioTrack = masterTrack.audioTracksSortedByDuration.get(masterTrack.audioTracksSortedByDuration.size() - 1);

            // These properties don't need to be bound with formal bindings.
            masterTrack.totalTimeLabel.setText(masterTrack.longestAudioTrack.totalTimeLabel.getText());
            masterTrack.timeSlider.setMax(masterTrack.longestAudioTrack.timeSlider.getMax());
        }
        else{
            masterTrack.longestAudioTrack = null;
            masterTrack.totalTimeLabel.setText("00:00");
            masterTrack.timeSlider.setMax(masterTrack.TIME_SLIDER_DEFAULT_MAX);
        }
    }

    static void refreshDisabledStatus(MasterTrack masterTrack){
        if(masterTrack.someTrackHasFile()){
            masterTrack.PPRButton.setDisable(false);
            masterTrack.syncButton.setDisable(false);
            masterTrack.timeSlider.setDisable(false);
        }
        else{
            timeSliderSetDefaultState(masterTrack.timeSlider);
            pprButtonSetDefaultState(masterTrack.PPRButton);
            syncButtonSetDefaultState(masterTrack.syncButton);
            masterTrack.synced = false;
            timeLabelSetDefaultState(masterTrack.currentTimeLabel);
        }
    }

    static void timeSliderSetDefaultState(Slider timeSlider){
        timeSlider.setValue(0.0);
        timeSlider.setDisable(true);
    }

    static void pprButtonSetDefaultState(Button PPRButton){
        PPRButton.setText("Play");
        PPRButton.setDisable(true);
    }

    static void syncButtonSetDefaultState(Button syncButton){
        syncButton.setText("Sync");
        syncButton.setDisable(true);
    }

    static void timeLabelSetDefaultState(Label timeLabel){
        timeLabel.setText("00:00 /");
    }

    static void refreshFocus(ArrayList<AudioTrack> audioTracks){
        for(AudioTrack track: audioTracks){
            if(track.focused) {
                track.focusTrack();
            }
        }
    }

    static void refreshUnFocus(ArrayList<AudioTrack> audioTracks){
        for(AudioTrack track: audioTracks){
            if(!track.focused) {
                track.undoFocus();
            }
        }
    }

    static void setSwitchDisabled(MasterTrack masterTrack){
        if(!masterTrack.isSomeTrackFocused()){
            masterTrack.switchButton.setDisable(true);
        }
        else if(!masterTrack.someTrackHasFile()){
            masterTrack.switchButton.setDisable(true);
        }
        else if(masterTrack.onlyOneTrackHasFile()){
            masterTrack.switchButton.setDisable(true);
        }
        else{
            masterTrack.switchButton.setDisable(false);
        }
    }

    static void setSeparatorVisibilities(ArrayList<AudioTrack> audioTracks){
        for(Track audioTrack: audioTracks){
            if(audioTrack.trackNumber == audioTracks.size()){
                audioTrack.lowerSeparator.setVisible(false);
            }
            else{
                audioTrack.lowerSeparator.setVisible(true);
            }
        }
    }

    static void refreshState(MasterTrack masterTrack){
        if(masterTrack.isSomeTrackFocused()){
            TrackStateManager.refreshFocus(masterTrack.audioTracks);
        }
        else{
            TrackStateManager.refreshUnFocus(masterTrack.audioTracks);
        }
        TrackStateManager.setSwitchDisabled(masterTrack);
    }
}
