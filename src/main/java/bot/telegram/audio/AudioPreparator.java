package bot.telegram.audio;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.job.FFmpegJob;
import net.bramp.ffmpeg.job.FFmpegJob.State;

public class AudioPreparator {
    public static final String SILENCE_START = "silence_start";
    public static final String SILENCE_END = "silence_end";
    public static final String SILENCE_DUR = "silence_duration";
    public static final String EXTRA_ARGS = "-af silencedetect=noise=-30dB:d=0.5, -f null";


    public List<String> splitAudio(String filename, int sizeLimitMb) {
        int curPart = 1;
        int sizeOfFiles = 1024 * 1024 * sizeLimitMb;

        byte[] buffer = new byte[sizeOfFiles];
        File file = new File(filename);
        List<String> newFilenames = new ArrayList<>();
        try (FileInputStream fin = new FileInputStream(file);
            BufferedInputStream bin = new BufferedInputStream(fin)) {

            int amount;
            while ((amount = bin.read(buffer)) > 0){
                File newFile = File.createTempFile("toSend"+curPart++, ".mp3");
                newFilenames.add(newFile.getAbsolutePath());
                try (FileOutputStream out = new FileOutputStream(newFile)) {
                    out.write(buffer, 0, amount);
                }
            }
            return newFilenames;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //TODO Find silence and split on it
    private void findSilence(File filename) {
        List<SilenceInfo> silenceList = new ArrayList<>();
        try {
            Files.lines(Paths.get(filename.toURI())).forEach(line -> {
                if (line.contains(SILENCE_START)) {
                    SilenceInfo silence = new SilenceInfo("3");
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private File writeSilenceToFile(String filename) {
        try {
            File outputFfmpeg = File.createTempFile("output", ".txt");
            FFmpeg fmpeg = new FFmpeg("path");
            FFprobe fprobe = new FFprobe("path");
            FFmpegBuilder builder = new FFmpegBuilder();
            builder.setInput(filename)
                .addOutput(outputFfmpeg.getAbsolutePath())
                .addExtraArgs(EXTRA_ARGS);

            FFmpegExecutor executor = new FFmpegExecutor(fmpeg, fprobe);
            FFmpegJob job = executor.createJob(builder);
            job.run();
            while ((job.getState() != (State.FINISHED)) || job.getState() != State.FAILED) {
                //
            }
            if (job.getState() == State.FAILED) {
                throw new RuntimeException();
            } else {
                return outputFfmpeg;
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String retrive(String url) {
        byte[] buffer = new byte[4096];
        int length;
        try {
            InputStream ain = new URL(url).openStream();
            File output = File.createTempFile("tempAudio", ".mp3");// + ain.getFormat().toString());
            FileOutputStream fout = new FileOutputStream(output);

            while ((length = ain.read(buffer)) > 0) {
                fout.write(buffer, 0, length);
            }

            ain.close();
            fout.close();

            return output.getAbsolutePath();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    class SilenceInfo {

        double start;
        double end;
        double duration;

        SilenceInfo(String start) {
            this.start = Double.parseDouble(start);
        }
    }
}
