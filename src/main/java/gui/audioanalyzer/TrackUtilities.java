package gui.audioanalyzer;

import javafx.scene.control.Button;
import javafx.util.Duration;
import java.util.ArrayList;

/**
 * Contains utility methods for Tracks.
 */
public class TrackUtilities {

    // ------------------------------------------------------------------------------------------------------------
    // --------------------------------------------- State Query Methods ------------------------------------------
    // ------------------------------------------------------------------------------------------------------------

    static boolean onlyOneTrackHasFile(ArrayList<AudioTrack> tracks){
        int tracksWithFiles = 0;
        for(AudioTrack track: tracks){
            if(trackHasFile(track)){
                tracksWithFiles++;
            }
        }
        return tracksWithFiles < 2;
    }

    static boolean isSomeTrackFocused(ArrayList<AudioTrack> tracks){
        for(AudioTrack track: tracks){
            if(track.focused) return true;
        }
        return false;
    }

    static boolean isSomeTrackPlaying(ArrayList<AudioTrack> tracks){
        for(AudioTrack track: tracks){
            if(track.PPRButton.getText().equals("Pause")) return true;
        }
        return false;
    }

    /**
     * Determines if at least one track in audioTracks has a valid file associated with it.
     * @return True if some track has a valid file, false otherwise.
     */
    static boolean someTrackHasFile(ArrayList<AudioTrack> tracks){
        for(AudioTrack track: tracks){
            if(trackHasFile(track)) return true;
        }
        return false;
    }

    /**
     * Determines if two tracks are equal.
     * @param trackOne The first track to compare.
     * @param trackTwo The second track to compare.
     * @return True if trackOne and trackTwo have the same trackNumber, false otherwise.
     */
    static boolean trackEquals(AudioTrack trackOne, AudioTrack trackTwo){
        return trackOne.trackNumber == trackTwo.trackNumber;
    }

    static int emptyTracks(ArrayList<AudioTrack> audioTracks){
        int emptyTracks = 0;
        for(AudioTrack track: audioTracks){
            if(!trackHasFile(track)){
                emptyTracks++;
            }
        }
        return emptyTracks;
    }

    static void compareTimeLabels(AudioTrack audioTrack){
        boolean updateMasterPPRText = false;
        if(audioTrack.masterTrack.synced && audioTrack.masterTrack.shortestAudioTrack != null && trackEquals(audioTrack, audioTrack.masterTrack.shortestAudioTrack)){
            updateMasterPPRText = true;
        }

        if(!updateMasterPPRText){
            if(audioTrack.masterTrack.synced) return;
            for(int i = 0; i < audioTrack.totalTimeLabel.getText().length(); i++){
                if(audioTrack.currentTimeLabel.getText().charAt(i) != audioTrack.totalTimeLabel.getText().charAt(i)){
                    audioTrack.atEndOfMedia = false;
                    if(audioTrack.isPlaying){
                        audioTrack.PPRButton.setText("Pause");
                    }
                    else{
                        audioTrack.PPRButton.setText("Play");
                    }
                    return;
                }
            }
            audioTrack.atEndOfMedia = true;
            audioTrack.PPRButton.setText("Restart");
        }
        else{
            for(int i = 0; i < audioTrack.totalTimeLabel.getText().length(); i++){
                if(audioTrack.currentTimeLabel.getText().charAt(i) != audioTrack.totalTimeLabel.getText().charAt(i)){
                    audioTrack.atEndOfMedia = false;
                    if(audioTrack.isPlaying){
                        audioTrack.PPRButton.setText("Pause");
                    }
                    else{
                        audioTrack.PPRButton.setText("Play");
                    }
                    return;
                }
            }
            audioTrack.atEndOfMedia = true;
            audioTrack.PPRButton.setText("Restart");
        }
    }

    static boolean trackHasFile(AudioTrack track){
        return (track.audioFile != null) && (track.media != null) && (track.mediaPlayer != null);
    }

    // ------------------------------------------------------------------------------------------------------------
    // --------------------------------------------- State Management Methods -------------------------------------
    // ------------------------------------------------------------------------------------------------------------

    static AudioTrack getFocusedTrack(ArrayList<AudioTrack> tracks){
        for(AudioTrack track: tracks){
            if(track.focused) return track;
        }
        return null;
    }

    static void sortAudioTracksByDuration(ArrayList<AudioTrack> tracks){
        int n = tracks.size();
        int i, j;
        AudioTrack temp;
        boolean swapped;
        for (i = 0; i < n - 1; i++) {
            swapped = false;
            for (j = 0; j < n - i - 1; j++) {
                if(tracks.get(j).mediaPlayer.getTotalDuration().toSeconds() > tracks.get(j + 1).mediaPlayer.getTotalDuration().toSeconds()) {
                    // Swap arr[j] and arr[j+1]
                    temp = tracks.get(j);
                    tracks.set(j, tracks.get(j + 1));
                    tracks.set(j + 1, temp);
                    swapped = true;
                }
            }

            // If no two elements were
            // swapped by inner loop, then break
            if (!swapped) break;
        }
    }

    static boolean removeTrackByNumber(ArrayList<AudioTrack> tracks, int trackNumber){
        for(int i = 0; i < tracks.size(); i++){
            if(tracks.get(i).trackNumber == trackNumber){
                tracks.remove(i);
                return true;
            }
        }
        return false;
    }

    static void forceFire(Button button){
        if(button.isDisable()){
            button.setDisable(false);
            button.fire();
            button.setDisable(true);
        }
        else{
            button.fire();
        }
    }

    /**
     * Nuclear option.
     * @param masterTrack
     */
    static void resetAllTracks(MasterTrack masterTrack){
        masterTrack.PPRButton.setText("Play");
        masterTrack.focusTrackLabel.setText("Focus Track: None");
        masterTrack.timeSlider.setValue(0.0);
        masterTrack.volumeSlider.setValue(1.0);
        for(AudioTrack track: masterTrack.audioTracks){
            if(trackHasFile(track)){
                track.mediaPlayer.seek(track.mediaPlayer.getStartTime());
                track.mediaPlayer.pause();
            }
            track.atEndOfMedia = false;
            track.isPlaying = false;
            track.isMuted = false;
            track.focused = false;
            track.pauseTime = 0.0;
            track.timeSlider.setValue(0.0);
            track.volumeSlider.setValue(1.0);
            track.PPRButton.setText("Play");
            track.trackLabel.borderProperty().set(null);
        }

        refreshDisabledStatus(masterTrack);
    }

    static void refreshDisabledStatus(MasterTrack masterTrack){
        if(someTrackHasFile(masterTrack.audioTracks)){
            masterTrack.PPRButton.setDisable(false);
            masterTrack.timeSlider.setDisable(false);
            masterTrack.volumeSlider.setDisable(false);
        }
        else{
            masterTrack.PPRButton.setDisable(true);
            masterTrack.timeSlider.setDisable(true);
            masterTrack.volumeSlider.setDisable(true);
        }

        for(AudioTrack track: masterTrack.audioTracks){
            if(trackHasFile(track) && !masterTrack.synced){
                track.PPRButton.setDisable(false);
                track.timeSlider.setDisable(false);
                track.volumeSlider.setDisable(false);
            }
            else{
                track.PPRButton.setDisable(true);
                track.timeSlider.setDisable(true);
                track.volumeSlider.setDisable(true);
            }
        }

        refreshSwitchDisabledStatus(masterTrack);
        refreshSyncDisabledStatus(masterTrack);
    }

    static void refreshTrackNumbers(ArrayList<AudioTrack> tracks){
        for(int i = 0; i < tracks.size(); i++){
            AudioTrack track = tracks.get(i);
            track.trackNumber = i + 1;
            track.trackLabel.setText("Track " + track.trackNumber);
        }
    }

    static void refreshShortestAudioTrack(MasterTrack masterTrack){
        if(masterTrack.audioTracksSortedByDuration.size() > 0){
            TrackUtilities.sortAudioTracksByDuration(masterTrack.audioTracksSortedByDuration);
            masterTrack.shortestAudioTrack = masterTrack.audioTracksSortedByDuration.get(0);

            // These properties don't need to be bound with formal bindings.
            masterTrack.totalTimeLabel.setText(masterTrack.shortestAudioTrack.totalTimeLabel.getText());
            masterTrack.timeSlider.setMax(masterTrack.shortestAudioTrack.mediaPlayer.getTotalDuration().toSeconds());
        }
        else{
            masterTrack.shortestAudioTrack = null;
            masterTrack.totalTimeLabel.setText("00:00");
            masterTrack.timeSlider.setMax(MasterTrack.TIME_SLIDER_DEFAULT_MAX);
        }
    }

    static void refreshSwitchDisabledStatus(MasterTrack masterTrack){
        if(!TrackUtilities.isSomeTrackFocused(masterTrack.audioTracks)){
            masterTrack.switchButton.setDisable(true);
        }
        else if(!TrackUtilities.someTrackHasFile(masterTrack.audioTracks)){
            masterTrack.switchButton.setDisable(true);
        }
        else if(TrackUtilities.onlyOneTrackHasFile(masterTrack.audioTracks)){
            masterTrack.switchButton.setDisable(true);
        }
        else{
            masterTrack.switchButton.setDisable(false);
        }
    }

    static void refreshSyncDisabledStatus(MasterTrack masterTrack){
        if(TrackUtilities.someTrackHasFile(masterTrack.audioTracks)){
            masterTrack.syncButton.setDisable(false);
        }
        else{
            masterTrack.syncButton.setDisable(true);
            masterTrack.syncButton.setText("Sync");
            masterTrack.synced = false;
        }
    }

    static void refreshMasterPPRText(MasterTrack masterTrack){
        if(masterTrack.synced){
            if(TrackUtilities.isSomeTrackPlaying(masterTrack.audioTracks)){
                masterTrack.PPRButton.setText("Pause");
            }
            else{
                masterTrack.PPRButton.setText("Play");
            }
            return;
        }

        // Out of all tracks with files, find out how many are playing, paused, and finished.
        int nonEmptyTracks = masterTrack.audioTracks.size() - TrackUtilities.emptyTracks(masterTrack.audioTracks);
        int finishedTracks = 0;
        int playingTracks = 0;
        int pausedTracks = 0;

        for(AudioTrack track: masterTrack.audioTracks){
            if(trackHasFile(track)){
                if(track.PPRButton.getText().equals("Play")){
                    pausedTracks++;
                }
                else if(track.PPRButton.getText().equals("Pause")){
                    playingTracks++;
                }
                else{
                    finishedTracks++;
                }
            }
        }
        if(pausedTracks == nonEmptyTracks){
            masterTrack.PPRButton.setText("Play");
        }
        else if(playingTracks == nonEmptyTracks){
            masterTrack.PPRButton.setText("Pause");
        }
        else if(finishedTracks == nonEmptyTracks){
            masterTrack.PPRButton.setText("Restart");
        }
        else {
            masterTrack.PPRButton.setText("Press All");
        }
    }

    static String getTime(Duration time){
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

    // ------------------------------------------------------------------------------------------------------------
    // --------------------------------------------- Debugging Methods --------------------------------------------
    // ------------------------------------------------------------------------------------------------------------

    static void printAudioTracksSortedByDuration(MasterTrack masterTrack){
        for(int i = 0; i < masterTrack.audioTracksSortedByDuration.size(); i++){
            AudioTrack track = masterTrack.audioTracksSortedByDuration.get(i);
            if(track.mediaPlayer == null){
                System.out.println("audioTracksSortedByDuration[" + i + "] = " + track.trackNumber + " (null)");
            }
            else{
                System.out.println("audioTracksSortedByDuration[" + i + "] = " + track.trackNumber + " (" + track.mediaPlayer.getTotalDuration().toSeconds() + ")");
            }
        }
    }

    static void printState(AudioTrack audioTrack){
        System.out.println("Track number: " + audioTrack.trackNumber);
        if(trackHasFile(audioTrack)){
            System.out.println("audioFile.getName(): " + audioTrack.audioFile.getName());
            System.out.println("media.toString(): " + audioTrack.media.toString());
            System.out.println("mediaPlayer.getStatus(): " + audioTrack.mediaPlayer.getStatus());
            System.out.println("atEndOfMedia: " + audioTrack.atEndOfMedia);
            System.out.println("isPlaying: " + audioTrack.isPlaying);
            System.out.println("isMuted: " + audioTrack.isMuted);
            System.out.println("pauseTime: " + audioTrack.pauseTime);
            System.out.println("focused: " + audioTrack.focused);
            System.out.println("synced: " + audioTrack.synced);

            System.out.println("volumeSlider.getValue(): " + audioTrack.volumeSlider.getValue());
            System.out.println("timeSlider.getValue(): " + audioTrack.timeSlider.getValue());
            System.out.println("mediaPlayer.getCurrentTime(): " + audioTrack.mediaPlayer.getCurrentTime().toSeconds());
        }
        else{
            System.out.println("<no file>");
        }
    }
}
