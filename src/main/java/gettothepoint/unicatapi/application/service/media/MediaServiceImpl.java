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
import java.util.Objects;

import static gettothepoint.unicatapi.application.service.media.MediaValidationUtil.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class MediaServiceImpl implements MediaService {

    @Value("${app.media.ffmpeg.path}")
    private String ffmpegPath;

    private static final String FILE_PREFIX = "unicat_artifact_";
    private static final String VIDEO_CODEC = "libx264";

    // Transition 음원 관련 상수
    private static final String TRANSITION_AUDIO_CLASSPATH = "assets/audio/transition/transition1.mp3";
    private static final String TRANSITION_AUDIO_PREFIX = "transition";

    // 전용 예외 클래스
    public static class MediaProcessingException extends RuntimeException {
        public MediaProcessingException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    // FFmpeg 경로 및 실행 권한 검사
    private void validateFfmpegPath() {
        if (ffmpegPath == null || ffmpegPath.isBlank()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "FFMPEG_PATH 환경변수가 설정되지 않았습니다. FFmpeg 경로를 확인하세요.");
        }
        File ffmpegFile = new File(ffmpegPath);
        if (!ffmpegFile.exists()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "FFmpeg 파일이 존재하지 않습니다. FFmpeg 경로를 확인하세요: " + ffmpegPath);
        }
        if (!ffmpegFile.isFile()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "FFMPEG_PATH가 올바른 파일이 아닙니다. FFmpeg 경로를 확인하세요: " + ffmpegPath);
        }
        if (!ffmpegFile.canExecute()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "FFmpeg 파일에 실행권한이 없습니다. 권한을 확인하세요: " + ffmpegPath);
        }
    }

    // FFmpeg 프로세스 실행 및 로그 출력
    private void executeFfmpegCommand(ProcessBuilder builder) {
        builder.redirectErrorStream(true);
        try {
            Process process = builder.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.info(line);
                }
            }
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "FFmpeg 실행 중 오류 발생. 종료코드: " + exitCode);
            }
        } catch (IOException e) {
            throw new MediaProcessingException("FFmpeg 실행 중 예외 발생", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new MediaProcessingException("FFmpeg 실행 중 인터럽트 발생", e);
        }
    }

    // 단일 이미지와 오디오를 합성하는 메서드
    @Override
    public File mergeImageAndAudio(File imageFile, File soundFile) {
        validateImageFile(imageFile.getAbsolutePath());
        validateAudioFile(soundFile.getAbsolutePath());
        validateFfmpegPath();
        String filename = FILE_PREFIX + Objects.hash(imageFile, soundFile);
        File outputFile = FileUtil.createTempFile(filename, ".mp4");

        List<String> command = new ArrayList<>();
        command.add(ffmpegPath);
        command.addAll(List.of(
                "-loop", "1",
                "-i", imageFile.getAbsolutePath(),
                "-i", soundFile.getAbsolutePath(),
                "-vf", "scale=1080:-1:force_original_aspect_ratio=decrease," +
                        "pad=1080:1920,pad=1080:1920:(ow-iw)/2:(oh-ih)/2,setsar=1",
                "-r", "30",
                "-c:v", VIDEO_CODEC,
                "-tune", "stillimage",
                "-c:a", "aac",
                "-b:a", "192k",
                "-pix_fmt", "yuv420p",
                "-shortest",
                "-y", outputFile.getAbsolutePath()
        ));
        ProcessBuilder builder = new ProcessBuilder(command);
        executeFfmpegCommand(builder);
        return outputFile;
    }

    // 배경 영상, 컨텐츠 이미지, 타이틀 이미지, 사운드를 합성하는 메서드
    @Override
    public File mergeImageAndAudio(File backgroundVideo, File contentImage, File titleImage, File soundFile) {
        validateFfmpegPath();
        validateVideoFile(backgroundVideo.getAbsolutePath());
        validateImageFile(contentImage.getAbsolutePath());
        validateImageFile(titleImage.getAbsolutePath());
        validateAudioFile(soundFile.getAbsolutePath());

        String filename = FILE_PREFIX + "merged_with_bg_" + System.currentTimeMillis();
        File outputFile = FileUtil.createTempFile(filename, ".mp4");
        double duration = getAudioDurationInSeconds(soundFile);

        String filter = "[1:v]scale=1080:-1:force_original_aspect_ratio=decrease," +
                "pad=1080:1080:(ow-iw)/2:(oh-ih)/2:black[content];" +
                "[2:v]scale=600:-1[title];" +
                "[0:v][content]overlay=(main_w-overlay_w)/2:(main_h-overlay_h)/2[tmp];" +
                "[tmp][title]overlay=(main_w-overlay_w)/2:100[outv]";

        List<String> command = List.of(
                ffmpegPath,
                "-stream_loop", "-1",
                "-i", backgroundVideo.getAbsolutePath(),
                "-loop", "1",
                "-i", contentImage.getAbsolutePath(),
                "-i", titleImage.getAbsolutePath(),
                "-i", soundFile.getAbsolutePath(),
                "-filter_complex", filter,
                "-map", "[outv]",
                "-map", "3:a",
                "-t", String.valueOf(duration),
                "-r", "30",
                "-c:v", VIDEO_CODEC,
                "-tune", "stillimage",
                "-c:a", "aac",
                "-b:a", "192k",
                "-pix_fmt", "yuv420p",
                "-shortest",
                "-y", outputFile.getAbsolutePath()
        );
        ProcessBuilder builder = new ProcessBuilder(command);
        executeFfmpegCommand(builder);
        return outputFile;
    }

    // 여러 영상을 합치는 메서드 (예시)
    @Override
    public File mergeVideosAndExtractVFR(List<File> files) {
        validateFfmpegPath();
        List<String> filePaths = files.stream().map(File::getAbsolutePath).toList();
        validateVideosFile(filePaths);

        File outputFile = FileUtil.createTempFile(FILE_PREFIX + "merged_videos_", ".mp4");
        long totalVideoDurationMs = 0;
        for (File file : files) {
            totalVideoDurationMs += getVideoDurationInMs(file);
        }
        double totalDurationSec = totalVideoDurationMs / 1000.0;

        List<String> command = new ArrayList<>();
        command.add(ffmpegPath);
        for (File file : files) {
            command.add("-i");
            command.add(file.getAbsolutePath());
        }
        // 전환 효과 오디오 파일을 위한 상수 사용
        File transitionSound = loadTransitionSoundFile(".mp3");
        command.add("-i");
        command.add(transitionSound.getAbsolutePath());

        StringBuilder filterComplex = new StringBuilder();
        for (int i = 0; i < files.size(); i++) {
            filterComplex.append("[").append(i).append(":v:0][").append(i).append(":a:0]");
        }
        filterComplex.append("concat=n=").append(files.size()).append(":v=1:a=1[outv][outa];");

        long totalDelay = 0;
        for (int i = 1; i < files.size(); i++) {
            totalDelay += getVideoDurationInMs(files.get(i - 1));
            filterComplex.append("[").append(files.size()).append(":a:0]adelay=")
                    .append(totalDelay).append("|").append(totalDelay)
                    .append("[sfx").append(i).append("];");
        }
        filterComplex.append("[outa]");
        for (int i = 1; i < files.size(); i++) {
            filterComplex.append("[sfx").append(i).append("]");
        }
        int totalInputs = 1 + (files.size() - 1);
        filterComplex.append("amix=inputs=").append(totalInputs).append(":duration=longest[out_finala]");

        command.add("-filter_complex");
        command.add(filterComplex.toString());
        command.addAll(List.of("-map", "[outv]", "-map", "[out_finala]", "-vsync", "vfr", "-c:v", VIDEO_CODEC,
                "-c:a", "aac", "-strict", "experimental", "-t", String.valueOf(totalDurationSec),
                "-y", outputFile.getAbsolutePath()));

        ProcessBuilder builder = new ProcessBuilder(command);
        executeFfmpegCommand(builder);
        return outputFile;
    }

    // ffprobe를 통해 오디오 파일의 길이를 초 단위로 구함 (전용 예외 사용)
    private double getAudioDurationInSeconds(File file) {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "ffprobe",
                    "-v", "error",
                    "-show_entries", "format=duration",
                    "-of", "default=noprint_wrappers=1:nokey=1",
                    file.getAbsolutePath()
            );
            Process process = pb.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line = reader.readLine();
                process.waitFor();
                return Double.parseDouble(line.trim());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new MediaProcessingException("ffprobe 인터럽트 발생", e);
        } catch (Exception e) {
            throw new MediaProcessingException("음성 길이 가져오기 실패: " + e.getMessage(), e);
        }
    }

    // ffprobe를 통해 비디오 파일의 길이를 밀리초 단위로 구함 (전용 예외 사용)
    private long getVideoDurationInMs(File file) {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "ffprobe",
                    "-v", "error",
                    "-select_streams", "v:0",
                    "-show_entries", "format=duration",
                    "-of", "default=noprint_wrappers=1:nokey=1",
                    file.getAbsolutePath()
            );
            Process process = pb.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line = reader.readLine();
                process.waitFor();
                return (long) (Double.parseDouble(line.trim()) * 1000);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new MediaProcessingException("ffprobe 인터럽트 발생", e);
        } catch (Exception e) {
            throw new MediaProcessingException("ffprobe duration 가져오기 실패: " + e.getMessage(), e);
        }
    }

    // Transition 음원 파일을 로드하는 전용 메서드
    private File loadTransitionSoundFile(String extension) {
        try {
            ClassPathResource resource = new ClassPathResource(TRANSITION_AUDIO_CLASSPATH);
            File tempFile = File.createTempFile(TRANSITION_AUDIO_PREFIX, extension);
            try (InputStream in = resource.getInputStream();
                 OutputStream out = new FileOutputStream(tempFile)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }
            return tempFile;
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "클래스패스 리소스 로딩 실패: " + TRANSITION_AUDIO_CLASSPATH, e);
        }
    }
}