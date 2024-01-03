package gui.audioanalyzer;

import java.io.File;

public class ManualTests {

    private static File testFileOne = new File("src\\test_audio_files\\00 Twelve_Inch (Original Mix).flac");
    private static File testFileTwo = new File("src\\test_audio_files\\01 Barbie Girl.m4a");
    private static File testFileThree = new File("src\\test_audio_files\\02 - get em.mp3");
    private static File testFileFour = new File("src\\test_audio_files\\Arctic Expedition (Instrumental) Mix 5.wav");
    private static File testFileFive = new File("src\\test_audio_files\\ASTR_205_Final_3_Lectures_Audio.mp3");

    private static void addTestFileToTrack(MasterTrack masterTrack, int testFileNumber, int trackNumber){
        AudioTrack trackToUpdate = masterTrack.audioTracks.get(trackNumber - 1);
        if(testFileNumber == 1) trackToUpdate.updateFile(testFileOne);
        if(testFileNumber == 2) trackToUpdate.updateFile(testFileTwo);
        if(testFileNumber == 3) trackToUpdate.updateFile(testFileThree);
        if(testFileNumber == 4) trackToUpdate.updateFile(testFileFour);
        if(testFileNumber == 5) trackToUpdate.updateFile(testFileFive);
    }

    private static void addNewTracks(MasterTrack masterTrack, int numberOfTracks){
        for(int i = 0; i < numberOfTracks; i++){
            masterTrack.addTrackButton.fire();
        }
    }

    /**
     * Test 1:
     * Press track one PPR.
     */
    static void setStateOne(MasterTrack masterTrack){
        masterTrack.audioTracks.get(0).updateFile(new File("src\\test_audio_files\\02 - get em.mp3"));
    }

    static void checkStateOne(MasterTrack masterTrack){
        if(!masterTrack.PPRButton.getText().equals("Pause")) return;
        if(!masterTrack.audioTracks.get(0).PPRButton.getText().equals("Pause")) return;
        System.out.println("Test 1 passed.");
    }

    /**
     * Test 2:
     * Press master PPR.
     */
    static void setStateTwo(MasterTrack masterTrack){
        masterTrack.audioTracks.get(0).updateFile(new File("src\\test_audio_files\\02 - get em.mp3"));
    }

    static void checkStateTwo(MasterTrack masterTrack){
        if(!masterTrack.PPRButton.getText().equals("Pause")) return;
        if(!masterTrack.audioTracks.get(0).PPRButton.getText().equals("Pause")) return;
        System.out.println("Test 2 passed.");
    }

    /**
     * Test 3:
     * Drag master time slider to end.
     * Press master track PPR.
     */
    // TODO: Fix bug:
    //  press sync
    //  press master ppr (or don't)
    //  focus track 4
    //  remove track 4
    //  focus track 1: track 2 is not muted and all volume slider are still synced.
    static void setStateThree(MasterTrack masterTrack){
        addNewTracks(masterTrack, 4);
        addTestFileToTrack(masterTrack, 2, 1);
        addTestFileToTrack(masterTrack, 3, 2);
        addTestFileToTrack(masterTrack, 4, 4);
    }

    static void checkStateThree(MasterTrack masterTrack){
        if(!masterTrack.PPRButton.getText().equals("Pause")) return;
        if(!masterTrack.audioTracks.get(0).PPRButton.getText().equals("Pause")) return;
        if(!masterTrack.audioTracks.get(1).PPRButton.getText().equals("Pause")) return;
        if(!masterTrack.audioTracks.get(3).PPRButton.getText().equals("Pause")) return;
        System.out.println("Test 3 passed.");
    }

    static void setStateFour(MasterTrack masterTrack){
        addNewTracks(masterTrack, 2);
        addTestFileToTrack(masterTrack, 2, 1);
        addTestFileToTrack(masterTrack, 4, 3);

        // Press sync.
        // Focus track 1.
    }
}
