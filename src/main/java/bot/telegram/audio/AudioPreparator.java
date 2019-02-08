package bot.telegram.audio;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import javax.inject.Inject;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;
import javazoom.spi.mpeg.sampled.file.MpegAudioFileReader;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;

public class AudioPreparator {
    private final String tempFolder;

    @Inject
    AudioPreparator(String tempFolder){
        if (tempFolder.isEmpty()){
            this.tempFolder = System.getProperty("java.io.temp");
        } else {
            this.tempFolder = tempFolder;
        }
    }

    private void splitAudio(String filename){
        try {
            FFmpeg fmpeg = new FFmpeg("path");
            FFprobe fprobe = new FFprobe("path");
            FFmpegBuilder builder = new FFmpegBuilder();
            builder.setInput(filename)
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String retrive(String url){
        byte[] buffer = new byte[4096];
        int length;
        try {
            MpegAudioFileReader mp = new MpegAudioFileReader();
            AudioInputStream ain = mp.getAudioInputStream(new URL(url));
            File output = new File(tempFolder+"/1" + ain.getFormat().toString());
            FileOutputStream fout = new FileOutputStream(output);

            while ((length = ain.read(buffer)) > 0){
                fout.write(buffer, 0 ,length);
            }

            ain.close();
            fout.close();

            return output.toString();

        } catch (UnsupportedAudioFileException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
