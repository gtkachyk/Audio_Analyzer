package gui.audioanalyzer;

import javafx.scene.control.Button;
import java.util.ArrayList;

/**
 * Contains utility methods for Tracks.
 */
public class TrackUtilities {

    static AudioTrack getFocusedTrack(ArrayList<AudioTrack> tracks){
        for(AudioTrack track: tracks){
            if(track.focused) return track;
        }
        return null;
    }

    static boolean onlyOneTrackHasFile(ArrayList<AudioTrack> tracks){
        int tracksWithFiles = 0;
        for(AudioTrack track: tracks){
            if(track.trackHasFile()){
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
            if(track.trackHasFile()) return true;
        }
        return false;
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
            if(!track.trackHasFile()){
                emptyTracks++;
            }
        }
        return emptyTracks;
    }

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

    static boolean removeTrackByNumber(ArrayList<AudioTrack> tracks, int trackNumber){
        for(int i = 0; i < tracks.size(); i++){
            if(tracks.get(i).trackNumber == trackNumber){
                tracks.remove(i);
                return true;
            }
        }
        return false;
    }

    static AudioTrack getAudioTrackByNumber(MasterTrack masterTrack, int trackNumber){
        for(AudioTrack track: masterTrack.audioTracks){
            if(track.trackNumber == trackNumber) return track;
        }
        return null;
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
            if(track.trackHasFile()){
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
            if(track.trackHasFile() && !masterTrack.synced){
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

        masterTrack.refreshSwitchDisabledStatus();
        masterTrack.refreshSyncDisabledStatus();
    }

    static void printAllVolumeSliderValues(ArrayList<AudioTrack> tracks){
        for(AudioTrack track: tracks){
            System.out.println("For track " + track.trackNumber + ", volumeSlider.getValue() = " + track.volumeSlider.getValue());
        }
        System.out.println("");
    }

    static void printAllTrackStates(ArrayList<AudioTrack> tracks){
        for(AudioTrack track: tracks){
            track.printState();
            System.out.println("");
//                        System.out.println("Track " + track.trackNumber + ": " + "audioLabel = " + track.audioLabel.getText() + ", focused = " + track.focused);
        }
        System.out.println("");
    }
}
