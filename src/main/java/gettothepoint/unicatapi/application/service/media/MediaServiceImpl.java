package gettothepoint.unicatapi.application.service.media;

import gettothepoint.unicatapi.common.util.FileUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static gettothepoint.unicatapi.application.service.media.MediaValidationUtil.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class MediaServiceImpl implements MediaService {

    private static String dynamicFfmpegPath() {
        return System.getProperty("FFMPEG_PATH");
    }

    private void validateFfmpegPath() {
        if (dynamicFfmpegPath() == null || dynamicFfmpegPath().isBlank()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "FFMPEG_PATH 환경 변수가 설정되지 않았습니다. FFmpeg 경로를 확인하세요.");
        }
        File ffmpegFile = new File(dynamicFfmpegPath());

        if (!ffmpegFile.exists()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "FFmpeg 파일이 존재하지 않습니다. FFmpeg 경로를 확인하세요: " + dynamicFfmpegPath());
        }

        if (!ffmpegFile.isFile()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "FFMPEG_PATH가 올바른 파일이 아닙니다. FFmpeg 경로를 확인하세요: " + dynamicFfmpegPath());
        }

        if (!ffmpegFile.canExecute()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "FFmpeg 파일에 실행 권한이 없습니다. 권한을 확인하세요: " + dynamicFfmpegPath());
        }

    }
    
    void executeFfmpegCommand(ProcessBuilder builder) {
        builder.redirectErrorStream(true);
        try {
            Process process = builder.start();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))
            ) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.info(line);
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "FFmpeg 실행 중 오류 발생. 종료 코드: " + exitCode);
            }
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "FFmpeg 실행 중 예외 발생", e);
        }
    }

    @Override
    public InputStream mergeImageAndSound(File imageFile, File soundFile) {
        validateImageFile(imageFile.getAbsolutePath());
        validateAudioFile(soundFile.getAbsolutePath());
        validateFfmpegPath();

        Integer fileHash = Objects.hash(imageFile, soundFile);
        String outputFilePath = FileUtil.getOrCreateTemp(fileHash, ".mp4").getAbsolutePath();

        List<String> command = new ArrayList<>();
        command.add(dynamicFfmpegPath());

        command.addAll(List.of(
                "-loop", "1",
                "-i", imageFile.getAbsolutePath(),
                "-i", soundFile.getAbsolutePath(),
                "-c:v", "libx264",
                "-tune", "stillimage",
                "-c:a", "aac",
                "-b:a", "192k",
                "-pix_fmt", "yuv420p",
                "-shortest",
                "-y",
                outputFilePath
        ));

        ProcessBuilder builder = new ProcessBuilder(command);
        executeFfmpegCommand(builder);

        try {
            return new FileInputStream(outputFilePath);
        } catch (FileNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "결과 파일을 찾을 수 없습니다: " + outputFilePath, e);
        }
    }

    @Override
    public InputStream mergeImageAndSound(InputStream imageStream, InputStream soundStream) {
        return null;
    }

    @Override
    public File mergeVideosAndExtractVFRFromFiles(List<File> files) {
        validateFfmpegPath();

        List<String> filePaths = files.stream()
                .map(File::getAbsolutePath)
                .collect(Collectors.toList());
        validateVideosFile(filePaths);

        Integer fileHash = Objects.hash(files);
        String outputFilePath = FileUtil.getOrCreateTemp(fileHash, ".mp4").getAbsolutePath();

        List<String> command = new ArrayList<>();
        command.add(dynamicFfmpegPath());

        for (File file : files) {
            command.add("-i");
            command.add(file.getAbsolutePath());
        }

        StringBuilder filterComplex = new StringBuilder();
        for (int i = 0; i < files.size(); i++) {
            filterComplex.append("[").append(i).append(":v:0]");
        }
        filterComplex.append("concat=n=").append(files.size()).append(":v=1:a=0[outv]");

        command.addAll(List.of(
                "-filter_complex", filterComplex.toString(),
                "-map", "[outv]",
                "-vsync", "vfr",
                "-c:v", "libx264",
                "-c:a", "aac",
                "-strict", "experimental",
                "-y",
                outputFilePath
        ));

        ProcessBuilder builder = new ProcessBuilder(command);
        executeFfmpegCommand(builder);

        return new File(outputFilePath);
    }

    @Override
    public InputStream mergeVideosAndExtractVFRFromInputStream(List<InputStream> files) {
        return null;
    }
}