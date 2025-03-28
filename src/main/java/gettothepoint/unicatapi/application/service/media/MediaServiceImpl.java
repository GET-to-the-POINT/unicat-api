package gettothepoint.unicatapi.application.service.media;

import gettothepoint.unicatapi.common.util.FileUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MediaServiceImpl implements MediaService {

    @Value("${app.media.ffmpeg.path}")
    private String ffmpegPath;

    private static final String FILE_PREFIX = "unicat_artifact_";
    private static final String VIDEO_CODEC = "libx264";

    private static final String TRANSITION_AUDIO_CLASSPATH = "assets/audio/transition/transition1.mp3";
    private static final String TRANSITION_AUDIO_PREFIX = "transition";

    public static class MediaProcessingException extends RuntimeException {
        public MediaProcessingException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    private void executeFfmpegCommand(ProcessBuilder builder) {
        builder.redirectErrorStream(true);
        try {
            Process process = builder.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) log.debug(line);
            }
            if (process.waitFor() != 0) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "FFmpeg 실행 실패");
            }
        } catch (IOException e) {
            throw new MediaProcessingException("FFmpeg 실행 중 IO 오류", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new MediaProcessingException("FFmpeg 인터럽트 발생", e);
        }
    }

    @Override
    public File mergeImageAndAudio(File imageFile, File soundFile) {

        File outputFile = FileUtil.createTempFile(".mp4");
        List<String> command = List.of(
                ffmpegPath,
                "-loop", "1",
                "-i", imageFile.getAbsolutePath(),
                "-i", soundFile.getAbsolutePath(),
                "-vf", "scale='if(gt(iw,1080),1080,iw)':'-1', crop='if(gt(in_w,1080),1080,in_w)':'if(gt(in_h,1080),1080,in_h)', setsar=1",
                "-r", "30", "-c:v", VIDEO_CODEC,
                "-tune", "stillimage", "-c:a", "aac", "-b:a", "192k",
                "-pix_fmt", "yuv420p", "-shortest", "-y", outputFile.getAbsolutePath()
        );
        executeFfmpegCommand(new ProcessBuilder(command));
        return outputFile;
    }

    @Override
    public File mergeImageAndAudio(File templateResource, File contentResource, File audioResource) {

        File outputFile = FileUtil.createTempFile(".mp4");
        double duration = getAudioDurationInSeconds(audioResource);

        String filter =
                "[1:v]scale='if(gt(iw,1080),1080,iw)':'-1'," +
                        "crop='if(gt(in_w,1080),1080,in_w)':'if(gt(in_h,1080),1080,in_h)',setsar=1[content];" +
                        "[0:v][content]overlay=(main_w-overlay_w)/2:(main_h-overlay_h)/2[tmp];";

        List<String> command = List.of(
                ffmpegPath,
                "-stream_loop", "-1", "-i", templateResource.getAbsolutePath(),
                "-loop", "1", "-i", contentResource.getAbsolutePath(),
                "-i", audioResource.getAbsolutePath(),
                "-filter_complex", filter,
                "-map", "[tmp]",
                "-map", "2:a",
                "-t", String.valueOf(duration),
                "-r", "30", "-c:v", VIDEO_CODEC, "-tune", "stillimage",
                "-c:a", "aac", "-b:a", "192k", "-pix_fmt", "yuv420p",
                "-shortest", "-y", outputFile.getAbsolutePath()
        );
        executeFfmpegCommand(new ProcessBuilder(command));
        return outputFile;
    }

    @Override
    public File mergeImageAndAudio(File templateResource, File contentResource, File titleResource, File audioResource) {

        File outputFile = FileUtil.createTempFile(".mp4");
        double duration = getAudioDurationInSeconds(audioResource);

        String filter =
                "[1:v]scale='if(gt(iw,1080),1080,iw)':'-1'," +
                        "crop='if(gt(in_w,1080),1080,in_w)':'if(gt(in_h,1080),1080,in_h)',setsar=1[content];" +
                        "[2:v]scale=600:-1[title];" +
                        "[0:v][content]overlay=(main_w-overlay_w)/2:(main_h-overlay_h)/2[tmp];" +
                        "[tmp][title]overlay=(main_w-overlay_w)/2:100[outv]";

        List<String> command = List.of(
                ffmpegPath, "-stream_loop", "-1", "-i", templateResource.getAbsolutePath(),
                "-loop", "1", "-i", contentResource.getAbsolutePath(),
                "-i", titleResource.getAbsolutePath(),
                "-i", audioResource.getAbsolutePath(),
                "-filter_complex", filter,
                "-map", "[outv]", "-map", "3:a",
                "-t", String.valueOf(duration),
                "-r", "30", "-c:v", VIDEO_CODEC, "-tune", "stillimage",
                "-c:a", "aac", "-b:a", "192k", "-pix_fmt", "yuv420p",
                "-shortest", "-y", outputFile.getAbsolutePath()
        );
        executeFfmpegCommand(new ProcessBuilder(command));
        return outputFile;
    }

    @Override
    public File mergeVideosAndExtractVFR(List<File> files) {

        File outputFile = FileUtil.createTempFile(".mp4");

        // 총 길이 계산
        long totalMs = 0;
        for (File f : files) totalMs += getVideoDurationInMs(f);
        double totalSec = totalMs / 1000.0;

        // transition 사운드
        File transitionSound = loadTransitionSoundFile();

        List<String> command = new ArrayList<>();
        command.add(ffmpegPath);
        for (File f : files) {
            command.add("-i");
            command.add(f.getAbsolutePath());
        }
        command.add("-i");
        command.add(transitionSound.getAbsolutePath());

        // 필터 생성
        StringBuilder filter = new StringBuilder();

        for (int i = 0; i < files.size(); i++) {
            filter.append("[").append(i).append(":v:0][").append(i).append(":a:0]");
        }

        filter.append("concat=n=").append(files.size()).append(":v=1:a=1[outv][outa];");

        long delay = 0;
        for (int i = 1; i < files.size(); i++) {
            delay += getVideoDurationInMs(files.get(i - 1));
            filter.append("[").append(files.size()).append(":a:0]adelay=")
                    .append(delay).append("|").append(delay)
                    .append(",volume=0.3[sfx").append(i).append("];");
        }

        filter.append("[outa]");
        for (int i = 1; i < files.size(); i++) {
            filter.append("[sfx").append(i).append("]");
        }

        filter.append("amix=inputs=")
                .append(1 + (files.size() - 1))
                .append(":duration=longest:dropout_transition=0:normalize=0[out_mixed];");

        filter.append("[out_mixed]volume=0.8[out_finala]");

        command.add("-filter_complex");
        command.add(filter.toString());

        command.addAll(List.of(
                "-map", "[outv]",
                "-map", "[out_finala]",
                "-vsync", "vfr",
                "-c:v", VIDEO_CODEC,
                "-c:a", "aac",
                "-strict", "experimental",
                "-t", String.valueOf(totalSec),
                "-y", outputFile.getAbsolutePath()
        ));

        executeFfmpegCommand(new ProcessBuilder(command));
        return outputFile;
    }

    private File loadTransitionSoundFile() {
        try {
            ClassPathResource resource = new ClassPathResource(TRANSITION_AUDIO_CLASSPATH);
            File tempFile = FileUtil.createTempFile(".mp3");
            try (InputStream in = resource.getInputStream();
                 OutputStream out = new FileOutputStream(tempFile)) {
                in.transferTo(out);
            }
            return tempFile;
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "클래스패스 리소스 로딩 실패: " + TRANSITION_AUDIO_CLASSPATH, e);
        }
    }

    private double getAudioDurationInSeconds(File file) {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "ffprobe", "-v", "error", "-show_entries", "format=duration",
                    "-of", "default=noprint_wrappers=1:nokey=1",
                    file.getAbsolutePath()
            );
            Process p = pb.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                String line = reader.readLine();
                p.waitFor();
                return Double.parseDouble(line.trim());
            }
        } catch (Exception e) {
            throw new MediaProcessingException("음성 길이 가져오기 실패", e);
        }
    }

    private long getVideoDurationInMs(File file) {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "ffprobe", "-v", "error", "-select_streams", "v:0",
                    "-show_entries", "format=duration",
                    "-of", "default=noprint_wrappers=1:nokey=1",
                    file.getAbsolutePath()
            );
            Process p = pb.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                String line = reader.readLine();
                p.waitFor();
                return (long) (Double.parseDouble(line.trim()) * 1000);
            }
        } catch (Exception e) {
            throw new MediaProcessingException("비디오 길이 가져오기 실패", e);
        }
    }

    public File extractThumbnail(File file) {
        if (!MediaValidationUtil.hasValidImageExtension(file.getName())) {
            throw new IllegalArgumentException("썸네일은 이미지 파일만 지원됩니다.");
        }
        return file;
    }
}
