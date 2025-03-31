package gettothepoint.unicatapi.application.service.media;

import gettothepoint.unicatapi.common.util.FileUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MediaServiceImpl implements MediaService {

    private static final String VIDEO_CODEC = "libx264";

    @Value("${app.media.ffmpeg.path}")
    private String ffmpegPath;

    private final TransitionSoundService transitionSoundService;

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
        double duration = transitionSoundService.getAudioDurationInSeconds(audioResource);

        String filter =
                "[1:v]scale=1080:-1,setsar=1," +
                        "crop=1080:if(gte(ih\\,1080)\\,1080\\,ih):0:if(gte(ih\\,1080)\\,(ih-1080)/2\\,0)[content];" +
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
        double duration = transitionSoundService.getAudioDurationInSeconds(audioResource);

        String filter =
                "[1:v]scale=1080:-1,setsar=1," +
                        "crop=1080:if(gte(ih\\,1080)\\,1080\\,ih):0:if(gte(ih\\,1080)\\,(ih-1080)/2\\,0)[content];" +
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

    public File mergeVideosAndExtractVFR(List<File> videos, List<File> transitionSounds) {
        File outputFile = FileUtil.createTempFile(".mp4");

        // 총 영상 길이 계산
        long totalMs = 0;
        for (File f : videos) {
            totalMs += transitionSoundService.getVideoDurationInMs(f);
        }
        double totalSec = totalMs / 1000.0;

        List<String> command = new ArrayList<>();
        command.add(ffmpegPath);

        // 입력 영상
        for (File f : videos) {
            command.add("-i");
            command.add(f.getAbsolutePath());
        }

        // 입력 효과음 (null인 경우 제외하고 add)
        for (File sound : transitionSounds) {
            if (sound != null) {
                command.add("-i");
                command.add(sound.getAbsolutePath());
            }
        }

        StringBuilder filter = new StringBuilder();

        // 영상 concat 필터
        for (int i = 0; i < videos.size(); i++) {
            filter.append("[").append(i).append(":v:0][").append(i).append(":a:0]");
        }
        filter.append("concat=n=").append(videos.size()).append(":v=1:a=1[outv][outa];");

        // 효과음 필터 (skip nulls)
        long delayMs = 0;
        int transitionInputOffset = videos.size(); // 효과음 input 시작 인덱스
        int actualSfxCount = 0;

        for (int i = 1; i < videos.size(); i++) {
            delayMs += transitionSoundService.getVideoDurationInMs(videos.get(i - 1));
            File transition = transitionSounds.get(i - 1); // section 1 기준이므로 i-1

            if (transition != null) {
                int transitionInputIndex = transitionInputOffset + actualSfxCount;
                filter.append("[")
                        .append(transitionInputIndex).append(":a:0]")
                        .append("adelay=").append(delayMs).append("|").append(delayMs)
                        .append(",volume=0.3[sfx").append(actualSfxCount).append("];");
                actualSfxCount++;
            }
        }

        // 믹싱
        filter.append("[outa]");
        for (int j = 0; j < actualSfxCount; j++) {
            filter.append("[sfx").append(j).append("]");
        }

        filter.append("amix=inputs=")
                .append(1 + actualSfxCount)
                .append(":duration=longest:dropout_transition=0:normalize=0[out_mixed];");

        // 최종 볼륨 조절
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
    public File extractThumbnail(File file) {
        if (!MediaValidationUtil.hasValidImageExtension(file.getName())) {
            throw new IllegalArgumentException("썸네일은 이미지 파일만 지원됩니다.");
        }
        return file;
    }
}
