package gettothepoint.unicatapi.application.service.media;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MediaServiceImpl implements MediaService {

    private static String dynamicFfmpegPath() {
        return System.getProperty("FFMPEG_PATH");
    }

    public String videos(List<String> targetFiles) {
        validateFfmpegPath();
        validateVideosFile(targetFiles);

        String homeDir = System.getProperty("user.home");
        String outputDirPath;

        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            outputDirPath = homeDir + "\\unicat_day";
        } else {
            outputDirPath = homeDir + "/.unicat.day";
        }

        File outputDir = new File(outputDirPath);
        if (!outputDir.exists() && !outputDir.mkdirs()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "출력 디렉토리 생성 실패: " + outputDir.getAbsolutePath());
        }

        String outputFile = outputDirPath + "/" + java.util.UUID.randomUUID() + ".mp4"; // macOS/Linux
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            outputFile = outputDirPath + "\\" + java.util.UUID.randomUUID() + ".mp4"; // Windows
        }
        if (!outputDir.exists() && !outputDir.mkdirs()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "출력 디렉토리 생성 실패: " + outputDir.getAbsolutePath());
        }

        List<String> command = new ArrayList<>();
        command.add(dynamicFfmpegPath());

        for (String file : targetFiles) {
            command.add("-i");
            command.add(file);
        }

        StringBuilder filterComplex = new StringBuilder();
        for (int i = 0; i < targetFiles.size(); i++) {
            filterComplex.append("[").append(i).append(":v:0]");
        }
        filterComplex.append("concat=n=").append(targetFiles.size()).append(":v=1:a=0[outv]");

        command.addAll(List.of(
                "-filter_complex", filterComplex.toString(),
                "-map", "[outv]",
                "-c:v", "libx264",
                "-c:a", "aac",
                "-strict", "experimental",
                outputFile
        ));

        ProcessBuilder builder = new ProcessBuilder(command);
        executeFfmpegCommand(builder);

        return outputFile;
    }

    public String audioAndVideo(String audioFilePath, String imageFilePath) {
        validateFfmpegPath();
        validateAudioFile(audioFilePath);
        validateImageFile(imageFilePath);
        String outputFile = "/tmp/" + java.util.UUID.randomUUID() + ".mp4"; // macOS/Linux
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            outputFile = "/tmp/" + java.util.UUID.randomUUID() + ".mp4"; // Windows
        }
        ProcessBuilder builder = new ProcessBuilder(
                dynamicFfmpegPath(),
                "-i", audioFilePath,
                "-i", imageFilePath,
                "-c:v", "copy",
                "-c:a", "aac",
                "-strict", "experimental",
                "-shortest",
                outputFile
        );

        executeFfmpegCommand(builder);

        return outputFile;
    }

//    public void createSectionVideos(Long projectId) throws IOException {
//        List<Section> sections = sectionRepository.findAllByProjectIdOrderBySortOrderAsc(projectId);
//
//        if (sections.isEmpty()) {
//            throw new IllegalArgumentException("해당 프로젝트의 섹션을 찾을 수 없습니다: " + projectId);
//        }
//
//        for (Section section : sections) {
//            File imageFile = supabaseStorageService.downloadFile(section.getImageUrl());
//            File audioFile = supabaseStorageService.downloadFile(section.getTtsUrl());
//
//            String outputFile = "/tmp/" + projectId + "_section_" + section.getId() + ".mp4";
//            imageAndAudio(imageFile.getAbsolutePath(), audioFile.getAbsolutePath(), outputFile);
//
//            File mergedFile = new File(outputFile);
//            MultipartFile multipartFile = new MultipartFileUtil(mergedFile, "section_video", "video/mp4");
//
//            // Supabase에 업로드
//            String supabaseVideoUrl;
//            try {
//                supabaseVideoUrl = supabaseStorageService.uploadFile(multipartFile);
//            } catch (Exception e) {
//                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
//                        "Supabase에 비디오 업로드 실패: " + e.getMessage(), e);
//            }
//
//            section.setVideoUrl(supabaseVideoUrl);
//            sectionRepository.save(section);
//
//            // 로컬 파일 정리
//            try {
//                Files.delete(imageFile.toPath());
//            } catch (IOException e) {
//                log.warn("이미지 파일 삭제 실패: {}", imageFile.getAbsolutePath(), e);
//            }
//
//            try {
//                Files.delete(audioFile.toPath());
//            } catch (IOException e) {
//                log.warn("오디오 파일 삭제 실패: {}", audioFile.getAbsolutePath(), e);
//            }
//
//            try {
//                Files.delete(mergedFile.toPath());
//            } catch (IOException e) {
//                log.warn("비디오 파일 삭제 실패: {}", mergedFile.getAbsolutePath(), e);
//            }
//
//            log.info("✅ 섹션 {}의 비디오가 생성 및 저장되었습니다: {}", section.getId(), supabaseVideoUrl);
//        }
//    }

    private void validateFfmpegPath() {
        String ffmpegPath = dynamicFfmpegPath();
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

    private void validateAudioFile(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Audio file path cannot be null or empty");
        }
        File audioFile = new File(filePath);
        if (!audioFile.exists() || !audioFile.isFile()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Audio file does not exist or is not a valid file: " + filePath);
        }
        String lowerAudio = filePath.toLowerCase();
        if (!(lowerAudio.endsWith(".mp3") || lowerAudio.endsWith(".aac")
                || lowerAudio.endsWith(".wav") || lowerAudio.endsWith(".flac"))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "지원되지 않는 오디오 파일 형식입니다: " + filePath);
        }
    }


    public void validateImageFile(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Image file path cannot be null or empty");
        }
        File imageFile = new File(filePath);
        if (!imageFile.exists() || !imageFile.isFile()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Image file does not exist or is not a valid file: " + filePath);
        }
        String lowerImage = filePath.toLowerCase();
        if (!(lowerImage.endsWith(".jpg") || lowerImage.endsWith(".jpeg")
                || lowerImage.endsWith(".png") || lowerImage.endsWith(".bmp"))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "지원되지 않는 이미지 파일 형식입니다: " + filePath);
        }
    }

    private void validateVideoFile(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Video file path cannot be null or empty");
        }
        File videoFile = new File(filePath);
        if (!videoFile.exists() || !videoFile.isFile()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Video file does not exist or is not a valid file: " + filePath);
        }
        String lowerVideo = filePath.toLowerCase();
        if (!(lowerVideo.endsWith(".mp4") || lowerVideo.endsWith(".mov")
                || lowerVideo.endsWith(".mkv") || lowerVideo.endsWith(".avi")
                || lowerVideo.endsWith(".flv"))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "지원되지 않는 비디오 파일 형식입니다: " + filePath);
        }

    }

    public void validateVideosFile(List<String> filePaths) {
        if (filePaths == null || filePaths.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Video file paths cannot be null or empty");
        }
        for (String filePath : filePaths) {
            validateVideoFile(filePath);
        }
    }

    @Override
    public File mergeImageAndSoundFromFile(File imageFile, File soundFile) {
        validateImageFile(imageFile.getAbsolutePath());
        validateAudioFile(soundFile.getAbsolutePath());
        validateFfmpegPath();

        String tempDir = System.getProperty("java.io.tmpdir");
        String outputFilePath = tempDir + File.separator + java.util.UUID.randomUUID().toString() + ".mp4";

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
                outputFilePath
        ));

        ProcessBuilder builder = new ProcessBuilder(command);
        executeFfmpegCommand(builder);

        return new File(outputFilePath);
    }

    @Override
    public InputStream mergeImageAndSoundFromInputStream(InputStream imageStream, InputStream soundStream) {
        return null;
    }

    @Override
    public File mergeVideosAndExtractVFRFromFiles(List<File> files) {
        validateFfmpegPath();

        List<String> filePaths = files.stream()
                .map(File::getAbsolutePath)
                .collect(Collectors.toList());
        validateVideosFile(filePaths);

        String tempDir = System.getProperty("java.io.tmpdir");
        String outputFilePath = tempDir + File.separator + java.util.UUID.randomUUID() + ".mp4";

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
        filterComplex.append("concat=n=").append(files.size()).append(":v=1:a=1[outv][outa]");

        command.addAll(List.of(
                "-filter_complex", filterComplex.toString(),
                "-map", "[outv]",
                "-vsync", "vfr",
                "-c:v", "libx264",
                "-c:a", "aac",
                "-strict", "experimental",
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