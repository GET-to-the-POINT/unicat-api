package gettothepoint.unicatapi.application.service.ffmpeg;

import gettothepoint.unicatapi.application.service.storage.SupabaseStorageService;
import gettothepoint.unicatapi.common.util.MultipartFileUtil;
import gettothepoint.unicatapi.domain.entity.dashboard.Section;
import gettothepoint.unicatapi.domain.repository.SectionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.*;
import java.nio.file.Files;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MergeService {

    private final SectionRepository sectionRepository;
    private final SupabaseStorageService supabaseStorageService;

    private static final String FFMPEG_PATH = System.getProperty("FFMPEG_PATH", "/opt/homebrew/bin/ffmpeg");

    public void videos(List<String> inputFiles, String outputFile) {
        validateFfmpegPath();

        File outputDir = new File(outputFile).getParentFile();
        File tempFileList = new File(outputDir, "file_list.txt");
        writeConcatFileList(tempFileList, inputFiles);

        ProcessBuilder builder = new ProcessBuilder(
                FFMPEG_PATH,
                "-f", "concat",
                "-safe", "0",
                "-i", tempFileList.getAbsolutePath(),
                "-vf", "scale=1080:1920:force_original_aspect_ratio=decrease,pad=1080:1920:(ow-iw)/2:(oh-ih)/2",
                "-c:v", "libx264",
                "-c:a", "aac",
                "-strict", "experimental",
                outputFile
        );
        executeFfmpegCommand(builder);

        if (!tempFileList.delete()) {
            log.warn("임시 파일 삭제 실패: {}", tempFileList.getAbsolutePath());
        }
        log.info("동영상 병합 성공! 저장 위치: {}", outputFile);
    }


    public void imageAndAudio(String imagePath, String audioPath, String outputFile) {
        validateFfmpegPath();

        ProcessBuilder builder = new ProcessBuilder(
                FFMPEG_PATH,
                "-loop", "1",
                "-i", imagePath,
                "-i", audioPath,
                "-vf", "scale=1080:1350:force_original_aspect_ratio=increase,crop=1080:1350,pad=1080:1920:(ow-iw)/2:(oh-ih)/2",
                "-c:v", "libx264",
                "-tune", "stillimage",
                "-c:a", "aac",
                "-b:a", "192k",
                "-movflags", "+faststart",
                "-shortest",
                outputFile
        );
        executeFfmpegCommand(builder);

        log.info("이미지와 오디오 병합 영상 생성 성공: {}", outputFile);
    }


    public void createSectionVideos(Long projectId) throws IOException {
        List<Section> sections = sectionRepository.findAllByProjectIdOrderBySortOrderAsc(projectId);

        if (sections.isEmpty()) {
            throw new IllegalArgumentException("해당 프로젝트의 섹션을 찾을 수 없습니다: " + projectId);
        }

        for (Section section : sections) {
            File imageFile = supabaseStorageService.downloadFile(section.getImageUrl());
            File audioFile = supabaseStorageService.downloadFile(section.getTtsUrl());

            String outputFile = "/tmp/" + projectId + "_section_" + section.getId() + ".mp4";
            imageAndAudio(imageFile.getAbsolutePath(), audioFile.getAbsolutePath(), outputFile);

            File mergedFile = new File(outputFile);
            MultipartFile multipartFile = new MultipartFileUtil(mergedFile, "section_video", "video/mp4");

            // Supabase에 업로드
            String supabaseVideoUrl;
            try {
                supabaseVideoUrl = supabaseStorageService.uploadFile(multipartFile);
            } catch (Exception e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Supabase에 비디오 업로드 실패: " + e.getMessage(), e);
            }

            section.setVideoUrl(supabaseVideoUrl);
            sectionRepository.save(section);

            // 로컬 파일 정리
            try {
                Files.delete(imageFile.toPath());
            } catch (IOException e) {
                log.warn("이미지 파일 삭제 실패: {}", imageFile.getAbsolutePath(), e);
            }

            try {
                Files.delete(audioFile.toPath());
            } catch (IOException e) {
                log.warn("오디오 파일 삭제 실패: {}", audioFile.getAbsolutePath(), e);
            }

            try {
                Files.delete(mergedFile.toPath());
            } catch (IOException e) {
                log.warn("비디오 파일 삭제 실패: {}", mergedFile.getAbsolutePath(), e);
            }

            log.info("✅ 섹션 {}의 비디오가 생성 및 저장되었습니다: {}", section.getId(), supabaseVideoUrl);
        }
    }

    private void validateFfmpegPath() {
        if (FFMPEG_PATH == null || FFMPEG_PATH.isBlank()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "FFMPEG_PATH 환경 변수가 설정되지 않았습니다. FFmpeg 경로를 확인하세요.");
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

    private void writeConcatFileList(File tempFileList, List<String> inputFiles) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFileList))) {
            for (String file : inputFiles) {
                writer.write("file '" + file + "'\n");
            }
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "출력 디렉토리 생성 실패: " + tempFileList.getParentFile().getAbsolutePath());
        }
    }
}