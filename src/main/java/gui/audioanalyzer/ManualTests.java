package gui.audioanalyzer;

import java.io.File;

public class ManualTests {

    private static final File testFileOne = new File("src\\test_audio_files\\00 Twelve_Inch (Original Mix).flac");
    private static final File testFileTwo = new File("src\\test_audio_files\\01 Barbie Girl.m4a");
    private static final File testFileThree = new File("src\\test_audio_files\\02 - get em.mp3");
    private static final File testFileFour = new File("src\\test_audio_files\\Arctic Expedition (Instrumental) Mix 5.wav");
    private static final File testFileFive = new File("src\\test_audio_files\\ASTR_205_Final_3_Lectures_Audio.mp3");
    private static final File testFileSix = new File("src\\test_audio_files\\04 Tomorrowland 2015 Mix.mp3");
    private static final File testFileSeven = new File("src\\test_audio_files\\16 - Fireball (Original Mix).mp3");
    private static final File testFileEight = new File("src\\test_audio_files\\34 Forbidden Voices (Original Mix).mp3");

    private static void addTestFileToTrack(MasterTrack masterTrack, int testFileNumber, int trackNumber){
        AudioTrack trackToUpdate = masterTrack.audioTracks.get(trackNumber - 1);
        if(testFileNumber == 1) trackToUpdate.updateFile(testFileOne);
        if(testFileNumber == 2) trackToUpdate.updateFile(testFileTwo);
        if(testFileNumber == 3) trackToUpdate.updateFile(testFileThree);
        if(testFileNumber == 4) trackToUpdate.updateFile(testFileFour);
        if(testFileNumber == 5) trackToUpdate.updateFile(testFileFive);
        if(testFileNumber == 6) trackToUpdate.updateFile(testFileSix);
        if(testFileNumber == 7) trackToUpdate.updateFile(testFileSeven);
        if(testFileNumber == 8) trackToUpdate.updateFile(testFileEight);
    }

    private static void addNewTracks(MasterTrack masterTrack, int numberOfTracks){
        for(int i = 0; i < numberOfTracks; i++){
            masterTrack.addTrackButton.fire();
        }
    }

    static void setState(MasterTrack masterTrack){
        addNewTracks(masterTrack, 1);
        addTestFileToTrack(masterTrack, 6, 1);
        addTestFileToTrack(masterTrack, 3, 2);
    }
}
