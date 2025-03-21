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
import java.util.stream.Collectors;

import static gettothepoint.unicatapi.application.service.media.MediaValidationUtil.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class MediaServiceImpl implements MediaService {

   @Value("${app.media.ffmpeg.path}")
    private String ffmpegPath;

    private final String filePrefix = "unicat_artifact_";

    private void validateFfmpegPath() {
        if (ffmpegPath == null || ffmpegPath.isBlank()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "FFMPEG_PATH 환경 변수가 설정되지 않았습니다. FFmpeg 경로를 확인하세요.");
        }
        File ffmpegFile = new File(ffmpegPath);

        if (!ffmpegFile.exists()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "FFmpeg 파일이 존재하지 않습니다. FFmpeg 경로를 확인하세요: " + ffmpegPath);
        }

        if (!ffmpegFile.isFile()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "FFMPEG_PATH가 올바른 파일이 아닙니다. FFmpeg 경로를 확인하세요: " + ffmpegPath);
        }

        if (!ffmpegFile.canExecute()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "FFmpeg 파일에 실행 권한이 없습니다. 권한을 확인하세요: " + ffmpegPath);
        }

    }

    void executeFfmpegCommand(ProcessBuilder builder) {
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
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "FFmpeg 실행 중 오류 발생. 종료 코드: " + exitCode);
            }
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "FFmpeg 실행 중 예외 발생", e);
        }
    }

    @Override
    public File mergeImageAndAudio(File imageFile, File soundFile) {
        validateImageFile(imageFile.getAbsolutePath());
        validateAudioFile(soundFile.getAbsolutePath());
        validateFfmpegPath();

        String filename = this.filePrefix + Objects.hash(imageFile, soundFile);
        String extension = ".mp4";
        File outputFile = FileUtil.createTempFile(filename, extension);

        List<String> command = new ArrayList<>();
        command.add(ffmpegPath);
        command.addAll(List.of("-loop", "1", "-i", imageFile.getAbsolutePath(), "-i", soundFile.getAbsolutePath(),"-vf","scale=1080:-1:force_original_aspect_ratio=decrease,pad=1080:1920,pad=1080:1920:(ow-iw)/2:(oh-ih)/2,setsar=1", "-r", "30","-c:v", "libx264", "-tune", "stillimage", "-c:a", "aac", "-b:a", "192k", "-pix_fmt", "yuv420p", "-shortest", "-y", outputFile.getAbsolutePath()));
        ProcessBuilder builder = new ProcessBuilder(command);
        executeFfmpegCommand(builder);

        return outputFile;
    }

    @Override
    public File mergeImageAndAudio(File imageFile, File soundFile, File titleImageFile) {
        validateImageFile(imageFile.getAbsolutePath());
        validateImageFile(titleImageFile.getAbsolutePath());
        validateAudioFile(soundFile.getAbsolutePath());
        validateFfmpegPath();

//        String filename = this.filePrefix + Objects.hash(imageFile, soundFile, titleImageFile);
//        File outputFile = FileUtil.createTempFile(filename, ".mp4");
        File outputFile = new File("/Users/yurim/Desktop/"+ "테스트ㅎ" + ".mp4");


        List<String> command = new ArrayList<>();
        command.add(ffmpegPath);

        // 입력: 배경 이미지 + 타이틀 이미지 + 음성
        command.addAll(List.of(
                "-loop", "1",
                "-i", imageFile.getAbsolutePath(),           // [0] 배경 이미지
                "-i", titleImageFile.getAbsolutePath(),      // [1] 타이틀 이미지
                "-i", soundFile.getAbsolutePath()            // [2] 음성
        ));

        // -vf → -filter_complex 사용: scale + overlay 타이틀
        String filter =
                "[0:v]scale=1080:-1:force_original_aspect_ratio=decrease," +
                        "pad=1080:1920:(ow-iw)/2:(oh-ih)/2,setsar=1[bg];" +
                        "[1:v]scale=600:-1[title];" +
                        "[bg][title]overlay=(main_w-overlay_w)/2:100[outv]";

        command.addAll(List.of(
                "-filter_complex", filter,
                "-map", "[outv]",
                "-map", "2:a",
                "-r", "30",
                "-c:v", "libx264",
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








    @Override
    public File mergeVideosAndExtractVFR(List<File> files) {
        validateFfmpegPath();

        List<String> filePaths = files.stream().map(File::getAbsolutePath).collect(Collectors.toList());
        validateVideosFile(filePaths);

        String outputFilePath = "/Users/yurim/Desktop/final102.mp4";
//        String filename = this.filePrefix + Objects.hash(files) + ".mp4";
//        String outputFilePath = FileUtil.getFile(filename).getAbsolutePath();

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

        File transitionSound = loadTempFileFromClasspath("assets/audio/transition/transition1.mp3", "transition", ".mp3");

        command.add("-i");
        command.add(transitionSound.getAbsolutePath());

        StringBuilder filterComplex = new StringBuilder();

        for (int i = 0; i < files.size(); i++) {
            filterComplex.append("[").append(i).append(":v:0][").append(i).append(":a:0]");
        }
        filterComplex.append("concat=n=").append(files.size()).append(":v=1:a=1[outv][outa]; ");

        long totalDelay = 0;

        for (int i = 1; i < files.size(); i++) {
            totalDelay += getVideoDurationInMs(files.get(i - 1));
            filterComplex.append("[").append(files.size())
                    .append(":a:0]adelay=")
                    .append(totalDelay).append("|").append(totalDelay)
                    .append("[sfx").append(i).append("]; ");
        }

        filterComplex.append("[outa]");
        for (int i = 1; i < files.size(); i++) {
            filterComplex.append("[sfx").append(i).append("]");
        }
        int totalInputs = 1 + (files.size() - 1);
        filterComplex.append("amix=inputs=").append(totalInputs)
                .append(":duration=longest[out_finala]");

        command.addAll(List.of(
                "-filter_complex", filterComplex.toString(),"-map", "[outv]","-map", "[out_finala]", "-vsync", "vfr", "-c:v", "libx264", "-c:a", "aac", "-strict", "experimental", "-t", String.valueOf(totalDurationSec), "-y", outputFilePath
        ));

        ProcessBuilder builder = new ProcessBuilder(command);
        executeFfmpegCommand(builder);

        return new File(outputFilePath);
    }

    public long getVideoDurationInMs(File file) {
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
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = reader.readLine();
            process.waitFor();
            return (long) (Double.parseDouble(line.trim()) * 1000); // 초 → ms
        } catch (Exception e) {
            throw new RuntimeException("ffprobe duration 가져오기 실패: " + e.getMessage(), e);
        }
    }

    private File loadTempFileFromClasspath(String classpathLocation, String prefix, String extension) {
        try {
            ClassPathResource resource = new ClassPathResource(classpathLocation);
            File tempFile = File.createTempFile(prefix, extension);
            try (InputStream in = resource.getInputStream(); OutputStream out = new FileOutputStream(tempFile)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }
            return tempFile;
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "클래스패스 리소스 로딩 실패: " + classpathLocation, e);
        }
    }


    public File mergeImageAndAudioWithBackground(File bgVideo, File imageFile, File titleImage, File soundFile) {
        validateVideoFile(bgVideo.getAbsolutePath());
        validateImageFile(imageFile.getAbsolutePath());
        validateImageFile(titleImage.getAbsolutePath());
        validateAudioFile(soundFile.getAbsolutePath());
        validateFfmpegPath();

        File outputFile = new File("/Users/yurim/Desktop/final_result_with_bg3.mp4");

        List<String> command = new ArrayList<>();
        command.add(ffmpegPath);

        command.addAll(List.of(
                "-stream_loop", "-1",
                "-i", bgVideo.getAbsolutePath(),     // [0] 배경 영상
                "-loop", "1",
                "-i", imageFile.getAbsolutePath(),   // [1] 메인 이미지
                "-i", titleImage.getAbsolutePath(),  // [2] 타이틀 이미지
                "-i", soundFile.getAbsolutePath()    // [3] 음성
        ));

        String filter =
                "[1:v]scale=1080:-1:force_original_aspect_ratio=decrease," +
                        "pad=1080:1080:(ow-iw)/2:(oh-ih)/2:black[main];" +
                        "[2:v]scale=600:-1[title];" +
                        "[0:v][main]overlay=(main_w-overlay_w)/2:(main_h-overlay_h)/2[tmp];" +
                        "[tmp][title]overlay=(main_w-overlay_w)/2:100[outv]";

        command.addAll(List.of(
                "-filter_complex", filter,
                "-map", "[outv]",
                "-map", "3:a",
                "-t", String.valueOf(getAudioDurationInSeconds(soundFile)),
                "-r", "30",
                "-c:v", "libx264",
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

    private double getAudioDurationInSeconds(File file) {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "ffprobe", "-v", "error",
                    "-show_entries", "format=duration",
                    "-of", "default=noprint_wrappers=1:nokey=1",
                    file.getAbsolutePath()
            );
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = reader.readLine();
            process.waitFor();
            return Double.parseDouble(line.trim());
        } catch (Exception e) {
            throw new RuntimeException("음성 길이 가져오기 실패: " + e.getMessage(), e);
        }
    }

}
